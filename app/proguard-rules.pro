-keepattributes *Annotation*, Signature, Exception
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}
-keep class com.mynotes.data.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.lifecycle.ViewModel
-dontwarn kotlinx.coroutines.**
-dontwarn androidx.room.**
