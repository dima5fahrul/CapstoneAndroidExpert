import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.capstoneandroidexpert.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        val props = Properties()
        val propsFile = rootProject.file("local.properties")
        if (propsFile.exists()) {
            props.load(propsFile.inputStream())
        }
        buildConfigField(
            "String",
            "TMDB_API_TOKEN",
            "\"${props.getProperty("tmdb.api.token", "")}\"",
        )
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            // Library modules must not enable minification; the consuming app
            // module's R8 run handles shrinking. Enabling it here causes the
            // app's R8 to fail with "Missing class" errors because core classes
            // are already obfuscated/removed before the app links against them.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.datastore.preferences)

    // Koin
    api(libs.koin.android)

    // Lifecycle
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.livedata.ktx)

    // Coroutines
    api(libs.kotlinx.coroutines.android)

    // Glide
    api(libs.glide)

    // Retrofit
    api(libs.retrofit)
    api(libs.retrofit.gson)
    api(libs.okhttp.logging)

    // Room
    api(libs.room.runtime)
    api(libs.room.ktx)
    ksp(libs.room.compiler)

    // SQLCipher
    implementation(libs.sqlcipher)
    implementation(libs.sqlite.ktx)

    // Root Detection
    implementation(libs.rootbeer)

    // Test
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
}

apply(plugin = "jacoco")

tasks.register<JacocoReport>("jacocoDebugTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val fileFilter =
        listOf(
            "**/R.class",
            "**/R\$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*",
            "**/*_Impl*",
            "**/di/**",
        )
    val kotlinDebugTree =
        fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
            exclude(fileFilter)
        }
    classDirectories.setFrom(files(kotlinDebugTree))
    sourceDirectories.setFrom(files("$projectDir/src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get()) { include("**/*.exec", "**/*.ec") },
    )
}
