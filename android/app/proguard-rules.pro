# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# ── Retrofit / GSON Rules ───────────────────────────────────────────────────
# Keep Gson data classes from being renamed by R8, ensuring successful JSON deserialization.
-keep class com.example.sudoku.data.api.** { *; }

# Keep methods that OkHttp/Retrofit uses via reflection
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }

# ── Coroutines ──────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
