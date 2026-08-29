# ProGuard / R8 Rules for Lise-Tayfa (com.cotx.app)

# 1. Release derlemelerinde Logcat çıktılarını kaldır (Görev 1.4)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** e(...);
    public static *** w(...);
}

# 2. Firebase Firestore serileştirme (reflection) modellerini koru
-keepclassmembers class com.cotx.app.data.model.** {
    <fields>;
    <init>();
}
-keep class com.cotx.app.data.model.** { *; }

# 3. Kotlin Metadata ve Annotations koruması
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# 4. Google Play Services & Firebase Auth Koruması (R8 Minify / Code 10 Fix)
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.auth.** { *; }
-dontwarn com.google.android.gms.**

# 5. Credential Manager + "Sign in with Google" (googleid) Koruması
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**
-dontwarn com.google.android.libraries.identity.googleid.**


