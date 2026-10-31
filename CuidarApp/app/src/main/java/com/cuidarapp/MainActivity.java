package com.cuidarapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.webkit.WebViewAssetLoader;

import com.cuidarapp.health.HealthBridge;
import com.cuidarapp.health.reminders.ReminderNotifier;
import com.cuidarapp.ui.bridge.AppBridge;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    
    private WebView webView;
    private AppBridge bridge;
    private WebViewAssetLoader assetLoader;
    private ValueCallback<Uri[]> pendingFileCallback;
    private boolean pageLoaded;
    private boolean openHealthWhenLoaded;

    private final ActivityResultLauncher<String> imagePicker =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (pendingFileCallback != null) {
                pendingFileCallback.onReceiveValue(uri != null ? new Uri[]{uri} : null);
                pendingFileCallback = null;
            }
        });

    private final ActivityResultLauncher<String> notificationPermission =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            granted -> notifyWebHealthChanged());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        webView = new WebView(this);
        setContentView(webView);
        
        setupAssetLoader();
        setupWebView();
        setupBackNavigation();

        openHealthWhenLoaded = getIntent().getBooleanExtra(ReminderNotifier.EXTRA_OPEN_HEALTH, false);
        ((CuidarApplication) getApplication()).setHealthChangedListener(this::notifyWebHealthChanged);
        
        webView.loadUrl("https://appassets.androidplatform.net/assets/web/index.html");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(ReminderNotifier.EXTRA_OPEN_HEALTH, false)) {
            openHealthWhenLoaded = true;
            if (pageLoaded) openHealthTab();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Permissões podem ter mudado nas configurações do Android.
        if (pageLoaded) notifyWebHealthChanged();
    }
    
    private void setupAssetLoader() {
        assetLoader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
            .build();
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setGeolocationEnabled(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        
        bridge = new AppBridge(this);
        webView.addJavascriptInterface(bridge, "AndroidBridge");
        webView.addJavascriptInterface(new HealthBridge(this), "CuidarHealth");
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                
                if (!isAllowedOrigin(uri)) {
                    return new WebResourceResponse("text/plain", "UTF-8", null);
                }
                
                return assetLoader.shouldInterceptRequest(uri);
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                
                if ("tel".equals(uri.getScheme())) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    return true;
                }
                
                return !isAllowedOrigin(uri);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                pageLoaded = true;
                if (openHealthWhenLoaded) openHealthTab();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                if (pendingFileCallback != null) {
                    pendingFileCallback.onReceiveValue(null);
                }
                pendingFileCallback = filePathCallback;
                try {
                    imagePicker.launch("image/*");
                } catch (Exception e) {
                    pendingFileCallback = null;
                    return false;
                }
                return true;
            }
        });
    }

    // A navegação é feita pela SPA; o JS decide se consome o "voltar" ou se o app deve fechar.
    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                webView.evaluateJavascript(
                    "(window.CuidarBack ? window.CuidarBack() : false)",
                    result -> {
                        if (!"true".equals(result)) {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                            setEnabled(true);
                        }
                    });
            }
        });
    }
    
    private boolean isAllowedOrigin(Uri uri) {
        String host = uri.getHost();
        return "appassets.androidplatform.net".equals(host);
    }
    
    @Override
    protected void onDestroy() {
        ((CuidarApplication) getApplication()).setHealthChangedListener(null);
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
    
    public void runJavaScript(String script) {
        runOnUiThread(() -> webView.evaluateJavascript(script, null));
    }

    // ==================== DIÁRIO DE SAÚDE ====================

    private void openHealthTab() {
        openHealthWhenLoaded = false;
        runJavaScript("window.CuidarOpenHealth && window.CuidarOpenHealth()");
    }

    private void notifyWebHealthChanged() {
        runJavaScript("window.CuidarHealthChanged && window.CuidarHealthChanged()");
    }

    public void requestNotificationPermission() {
        runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= 33) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                openNotificationSettings();
            }
        });
    }

    public void openNotificationSettings() {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startSettings(intent);
    }

    public void openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startSettings(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:" + getPackageName())));
        }
    }

    public void openBatterySettings() {
        startSettings(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
    }

    private void startSettings(Intent intent) {
        runOnUiThread(() -> {
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName())));
            }
        });
    }

    /** Compartilhamento só por ação do usuário, pela folha de compartilhamento do Android. */
    public void shareFile(File file, String mimeType, String title) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent send = new Intent(Intent.ACTION_SEND)
            .setType(mimeType)
            .putExtra(Intent.EXTRA_STREAM, uri)
            .putExtra(Intent.EXTRA_SUBJECT, title)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        send.setClipData(ClipData.newRawUri(title, uri));
        runOnUiThread(() -> startActivity(Intent.createChooser(send, title)));
    }
}
