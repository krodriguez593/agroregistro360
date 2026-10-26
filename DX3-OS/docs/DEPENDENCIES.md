# Dependencias

## En tiempo de ejecución (dentro del APK)

| Dependencia | Motivo |
|---|---|
| Framework Android (API 21+) | Toda la UI y los servicios |
| `kotlin-stdlib` 2.0.21 | Reducido por R8 |
| `:core` | Lógica propia |

Sin AndroidX, sin Compose, sin librerías de red, sin analítica.

## Para compilar

| Herramienta | Versión | Origen |
|---|---|---|
| JDK | 17+ (probado con 21) | Sistema |
| Gradle | 8.14.3 | Wrapper (`./gradlew`) |
| Kotlin Gradle Plugin | 2.0.21 | Gradle Plugin Portal |
| Android SDK Platform | 35 | `sdkmanager "platforms;android-35"` |
| Android Build-Tools | 35.0.1 | `sdkmanager "build-tools;35.0.1"` |
| Android Platform-Tools (adb) | reciente | `sdkmanager platform-tools` |
| Android Gradle Plugin (opcional) | 8.7.3 | Google Maven — solo para `:app` vía Gradle |
| `kotlinc` (opcional) | 2.0.21 | GitHub Releases — solo para `tools/build/build-apk.sh` |

## Para pruebas

| Dependencia | Uso |
|---|---|
| JUnit 4.13.2 | Pruebas de `:core` (Maven Central) |

## Comandos

```bash
# Pruebas de la lógica (cualquier máquina con JDK)
./gradlew -Pdx3.withAndroid=false :core:test

# APK con Android Studio / AGP (necesita Google Maven y local.properties con sdk.dir)
./gradlew :app:assembleDebug

# APK solo con el SDK (sin Google Maven)
ANDROID_HOME=/ruta/sdk KOTLIN_HOME=/ruta/kotlinc tools/build/build-apk.sh

# Comprobaciones de seguridad
tools/checks/check-safety.sh
```
