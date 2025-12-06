-keep class com.atelierversace.** { *; }

-repackageclasses ''
-allowaccessmodification

-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

-keep class com.google.firebase.** { *; }
-keep class com.google.ai.** { *; }

-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**

-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

-dontwarn java.lang.management.**

-dontwarn okhttp3.internal.**
-keep class okhttp3.internal.** { *; }

-dontwarn io.ktor.util.debug.**
-keep class io.ktor.util.debug.** { *; }

-keep class okhttp3.internal.sse.** { *; }
-dontwarn okhttp3.internal.sse.**

-dontwarn javax.management.**
-dontwarn java.beans.**