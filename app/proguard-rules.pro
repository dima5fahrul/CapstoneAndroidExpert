# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Koin ViewModels
-keep class com.example.capstoneandroidexpert.di.** { *; }

# Core module — keep all public API classes used by app module
-keep class com.example.capstoneandroidexpert.core.** { *; }

# Navigation safe args
-keep class * extends androidx.navigation.NavArgs { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.** { *; }
