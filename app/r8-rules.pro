# R8 Full Mode Optimizations
# This file contains additional R8-specific optimizations

# Enable aggressive optimization
-optimizationpasses 5
-overloadaggressively
-repackageclasses ''
-allowaccessmodification
-mergeinterfacesaggressively

# Remove Kotlin metadata that isn't needed at runtime
-checkdiscard class kotlin.Metadata

# Inline all methods that are only called once
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(...);
    static void checkNotNullParameter(...);
    static void checkExpressionValueIsNotNull(...);
    static void checkNotNullExpressionValue(...);
    static void checkReturnedValueIsNotNull(...);
    static void checkFieldIsNotNull(...);
    static void throwUninitializedPropertyAccessException(...);
    static void throwNpe();
}

# Remove all check casts
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkNotNull(...);
}

# Optimize Compose runtime
-assumenosideeffects class androidx.compose.runtime.ComposerKt {
    void sourceInformation(...);
    void sourceInformationMarkerStart(...);
    void sourceInformationMarkerEnd(...);
    boolean isTraceInProgress();
    void traceEventStart(...);
    void traceEventEnd();
}

# Remove verbose coroutine debugging
-assumenosideeffects class kotlinx.coroutines.debug.** {
    *;
}

# Optimize collections
-optimizationpasses 5
-flattenpackagehierarchy
