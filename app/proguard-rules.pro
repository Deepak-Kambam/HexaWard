# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Optimizations
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Hilt/Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class kotlin.Metadata { *; }
-keep class kotlin.jvm.functions.** { *; }
-keep class kotlin.coroutines.** { *; }

# WorkManager
-keep class androidx.work.impl.WorkManagerInitializer { *; }
-keep class androidx.work.Worker { *; }

# Compose
-keep class androidx.compose.** { *; }
-if class androidx.compose.ui.platform.ComposeView {
    void setCompositionContext(androidx.compose.runtime.CompositionContext);
}
-keep class androidx.compose.runtime.CompositionContext { *; }

# ViewModel
-keep class androidx.lifecycle.ViewModel { *; }
-keep class androidx.lifecycle.ViewModelProvider { *; }

# Data classes
-keepclasseswithmembers class * {
    public <init>(...);
}

# Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Parcelable
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Allow obfuscation of android.support.v7.internal.view.menu.**
# to avoid problem on Samsung devices (see issue #3)
# https://code.google.com/p/android/issues/detail?id=78377
-keep class !android.support.v7.internal.view.menu.**,android.support.** {*;}

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from shaking annotations
-keep class **$$ExternalSyntheticLambda* { *; }
-assumenosideeffects class kotlin.jvm.internal.Intrinsics { *; }

# Enable full mode for better optimization
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}