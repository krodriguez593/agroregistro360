// DX3 OS — build multi-módulo.
//
// :core  -> Kotlin puro (JVM). Se compila y prueba en cualquier máquina con JDK 17+.
// :app   -> App launcher Android (Android Gradle Plugin). Requiere acceso a Google Maven
//           (dl.google.com) y un Android SDK. Se puede excluir con -Pdx3.withAndroid=false
//           (por ejemplo en entornos sin acceso a Google Maven; ver tools/build/build-apk.sh).

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        if (providers.gradleProperty("dx3.withAndroid").orNull != "false") google()
    }
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.0.21"
        id("org.jetbrains.kotlin.android") version "2.0.21"
        id("com.android.application") version "8.7.3"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        if (providers.gradleProperty("dx3.withAndroid").orNull != "false") google()
    }
}

rootProject.name = "DX3-OS"

include(":core")
if (providers.gradleProperty("dx3.withAndroid").orNull != "false") {
    include(":app")
}

