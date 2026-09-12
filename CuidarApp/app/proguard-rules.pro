# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in android/sdk/tools/proguard/proguard-android.txt

# Keep Room entities
-keep class com.cuidarapp.model.** { *; }

# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep JavaScript interface methods
-keepclassmembers class com.cuidarapp.ui.bridge.AppBridge {
    @android.webkit.JavascriptInterface <methods>;
}
