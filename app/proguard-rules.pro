# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# WebView JS bridge (ForgotPasswordScreen)
-keepclassmembers class com.kanyandula.nyasa.ui.auth.WebAppInterface {
   public *;
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- Retrofit ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-if interface * { @retrofit2.http.* public *** ...(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# --- kotlinx-serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.kanyandula.nyasa.**$$serializer { *; }
-keepclassmembers class com.kanyandula.nyasa.** { *** Companion; }
-keepclasseswithmembers class com.kanyandula.nyasa.** { kotlinx.serialization.KSerializer serializer(...); }
-dontwarn sun.misc.**

# --- OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**

# --- Parcelize ---
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
