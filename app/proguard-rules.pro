# Reglas oficiales de ProGuard / R8 para Kuota App (PRD §15 y AGENTS.md)

# 1. Preservar modelos de dominio serializables
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName *;
}
-keepattributes *Annotation*,InnerClasses,Signature
-dontwarn kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *; }

# Modelos propios de Kuota
-keep class com.kuota.core.model.** { *; }
-keep class com.kouta.app.domain.model.** { *; }
-keep class com.kouta.app.data.local.** { *; }

# 2. Hilt / Dagger Dependency Injection
-keep class * extends dagger.hilt.internal.UnsafeCasts { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}
-keepclasseswithmembers class * {
    @dagger.Provides <methods>;
}
-dontwarn dagger.hilt.**
-dontwarn javax.annotation.**

# 3. Firebase SDKs (Auth, Firestore, Functions, FCM, Storage)
-keepattributes *JavascriptInterface*
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName *;
    @com.google.firebase.firestore.Exclude *;
    @com.google.firebase.firestore.ServerTimestamp *;
}
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# 4. Google ML Kit (Text Recognition & Vision)
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.vision.** { *; }
-dontwarn com.google.mlkit.**

# 5. ZXing QR Code
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# 6. Sentry Crash Reporting
-keepattributes LineNumberTable,SourceFile
-keep class io.sentry.** { *; }
-dontwarn io.sentry.**

# 7. OkHttp / Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# 8. Coil Image Loading
-keep class coil.** { *; }
-dontwarn coil.**
