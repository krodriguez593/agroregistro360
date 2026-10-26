// Build estándar con Android Gradle Plugin (Android Studio / CI con acceso a Google Maven).
// En entornos sin Google Maven usar tools/build/build-apk.sh, que compila las MISMAS fuentes
// con las herramientas del SDK (aapt2, kotlinc, d8/R8, apksigner).
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ec.dx3os.launcher"
    compileSdk = 35

    defaultConfig {
        applicationId = "ec.dx3os.launcher"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-stage1"
    }

    sourceSets["main"].java.srcDirs("src/main/kotlin")

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
}

dependencies {
    // Sin AndroidX ni librerías de terceros: solo el framework de Android y :core.
    implementation(project(":core"))
}
