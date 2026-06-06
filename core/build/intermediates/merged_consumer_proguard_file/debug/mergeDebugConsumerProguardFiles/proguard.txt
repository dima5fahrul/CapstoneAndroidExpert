# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson / JSON serialization
-keep class com.example.capstoneandroidexpert.core.data.source.remote.response.** { *; }
-keep class com.example.capstoneandroidexpert.core.data.source.local.entity.** { *; }
-keep class com.example.capstoneandroidexpert.core.domain.model.** { *; }
-keepattributes SerializedName

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Koin
-keep class org.koin.** { *; }
-keepnames class * implements org.koin.core.module.Module

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
