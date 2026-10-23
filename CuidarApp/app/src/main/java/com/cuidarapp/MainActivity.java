package com.cuidarapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.webkit.WebViewAssetLoader;

import com.cuidarapp.ui.bridge.AppBridge;

public class MainActivity extends AppCompatActivity {
    
    private WebView webView;
    private AppBridge bridge;
    private WebViewAssetLoader assetLoader;
    private ValueCallback<Uri[]> pendingFileCallback;

    private final ActivityResultLauncher<String> imagePicker =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (pendingFileCallback != null) {
                pendingFileCallback.onReceiveValue(uri != null ? new Uri[]{uri} : null);
                pendingFileCallback = null;
            }
        });
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        webView = new WebView(this);
        setContentView(webView);
        
        setupAssetLoader();
        setupWebView();
        setupBackNavigation();
        
        webView.loadUrl("https://appassets.androidplatform.net/assets/web/index.html");
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
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
    
    public void runJavaScript(String script) {
        runOnUiThread(() -> webView.evaluateJavascript(script, null));
    }
}
