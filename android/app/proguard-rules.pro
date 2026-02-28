# Default ProGuard rules for Ben's Bible

# Keep Compose runtime
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep Room entities
-keep class com.bensbible.app.data.** { *; }

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class com.bensbible.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.bensbible.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
