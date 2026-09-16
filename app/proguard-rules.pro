# SingBord ProGuard / R8 Rules

# Room Database keep rules
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Compose runtime
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.runtime.**

# Keep line numbers for accurate stack traces if crashes happen
-keepattributes SourceFile,LineNumberTable

