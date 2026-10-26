# Etapa 1 — Informe de viabilidad y entorno

Fecha: 2026-10-26

## 1. Inspección del entorno de desarrollo

| Elemento | Resultado |
|---|---|
| Repositorio | `krodriguez593/agroregistro360` (proyecto web no relacionado). DX3 OS se creó en la carpeta `DX3-OS/` sin tocar el resto. **Recomendación:** moverlo a un repositorio propio |
| SO / CPU / RAM | Linux x86-64 en contenedor, 4 núcleos, 15 GB RAM |
| JDK | OpenJDK 21 |
| Gradle | 8.14.3 (wrapper incluido) |
| Kotlin | 2.0.21 (plugin Gradle desde Gradle Plugin Portal; `kotlinc` desde GitHub Releases) |
| Android SDK | No instalado. `dl.google.com` **bloqueado** por la política de red. Se obtuvo `platforms/android-35`, `build-tools/35.0.1` y `platform-tools` copiándolos de la imagen Docker pública `cimg/android:2025.10` |
| Google Maven (`maven.google.com` → `dl.google.com`) | **Bloqueado.** Sin Android Gradle Plugin ni AndroidX en este entorno |
| Maven Central | Accesible (con límites de peticiones intermitentes) |
| Emulador Android | No viable: sin `/dev/kvm` ni binder en el contenedor |
| Dispositivo ADB | Ninguno conectado (`adb get-state` → sin dispositivo) |

Consecuencias:
- La app se escribió con el framework de Android sin AndroidX (también conviene por ligereza).
- `app/build.gradle.kts` (AGP) **no se pudo ejecutar aquí**; el APK se compiló con
  `tools/build/build-apk.sh` (aapt2 + kotlinc + R8 + apksigner) a partir de las mismas fuentes.
- Se intentaron pruebas de interfaz con Robolectric, pero dependen de `androidx.test:monitor`
  (solo en Google Maven). Se retiraron en vez de dejar pruebas sin ejecutar. Pendiente para un
  entorno con Google Maven o un dispositivo real.

## 2. Resultados verificados en esta etapa

| Comprobación | Resultado |
|---|---|
| `./gradlew -Pdx3.withAndroid=false :core:test` | **27 pruebas, 0 fallos** |
| Compilación Kotlin de la app (`-Werror`) | OK |
| `tools/build/build-apk.sh` | OK → `build/apk/dx3os-debug.apk`, **75 448 bytes**, firma verificada con `apksigner verify` |
| `aapt2 dump badging` | paquete `ec.dx3os.launcher`, minSdk 21, targetSdk 34, 3 permisos de uso + 1 de enlace de servicio |
| `tools/checks/check-safety.sh` | OK (27 comandos de diagnóstico de solo lectura; sin acceso CAN/serie; sin INTERNET). Pruebas negativas (`setprop`, `reboot`, `echo > /sys`) detectadas correctamente |
| Ejecución de la app en un dispositivo | **No realizada** (sin dispositivo ni emulador) |

## 3. Viabilidad

- **Opción A (launcher) — recomendada**, condicionada a que la radio sea Android y permita
  instalar APK. Se puede probar ya en cualquier tableta Android 5.0+.
- B depende del diagnóstico. C y D descartadas sin código fuente del fabricante (ver ARCHITECTURE.md).
- Android Auto: **no habilitable por software**; ver ANDROID_AUTO.md.

## 4. Comandos de diagnóstico seguros

Todos incluidos en `tools/diagnostics/dx3-readonly-diag.sh`:

```
adb devices                                  # listar (ejecutar en el PC)
adb shell getprop                            # propiedades del sistema
adb shell cat /proc/cpuinfo /proc/meminfo    # SoC y RAM
adb shell df -h ; cat /proc/partitions       # almacenamiento (solo lectura)
adb shell wm size ; wm density               # resolución
adb shell getevent -p                        # dispositivos de entrada (no captura toques)
adb shell dumpsys input | display | bluetooth_manager | audio | media.camera | media_session
adb shell pm list features                   # capacidades de hardware
adb shell cat /proc/modules                  # drivers cargados
adb shell 'getprop | grep ro.boot.'          # arranque (sin tocar el bootloader)
```

Prohibidos en esta fase: `setprop`, `reboot`, `fastboot`, `dd`, `rm`, `install`, `push`, `su`,
`settings put`, `input`, `sendevent`, `am`, `pm clear/disable`, cualquier "unlock"/"flash".

## 5. Pendientes y errores conocidos

1. Identificar la radio (ver HARDWARE_REPORT.md §6: información que se necesita del propietario).
2. Ejecutar la app en un dispositivo real y medir arranque y memoria (Ajustes → Rendimiento).
3. Validar `app/build.gradle.kts` con AGP en una máquina con Google Maven.
4. Pruebas de interfaz automatizadas (Robolectric o instrumentadas) — bloqueadas en este entorno.
5. Textos en español codificados en Kotlin; pasar a `strings.xml` si se necesita otro idioma.
6. Controles multimedia de la tarjeta de Inicio: 60–64 dp (por debajo del objetivo de 76 dp)
   por espacio; en la pantalla Música son de 84–104 dp. Revisar con la resolución real.
7. Lint de Android no ejecutado (se distribuye por Google Maven).
