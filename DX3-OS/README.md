# DX3 OS

Sistema multimedia ligero para el **Soueast DX3 2020**: un launcher Android con interfaz
automotriz oscura (grafito + cian), pensado para radios con pocos recursos.

> **Estado: Etapa 1 (prototipo).** La radio del vehículo **todavía no está identificada**.
> El prototipo funciona en cualquier Android 5.0+ en horizontal y no modifica ningún firmware.
> No es Android Auto ni lo imita (ver [docs/ANDROID_AUTO.md](docs/ANDROID_AUTO.md)).

## Qué hay implementado (real, no maqueta)

| Función | Implementación | Limitación actual |
|---|---|---|
| Launcher (HOME) | Actividad elegible como pantalla de inicio | Probado solo en compilación |
| Reloj y fecha | Español, 12/24 h, tick por minuto | — |
| Barra inferior | Inicio · Navegación · Música · Teléfono · Aplicaciones · Ajustes | — |
| Música | Sesión multimedia activa del sistema (MediaSession): título, artista, progreso, anterior/reproducir/siguiente | Requiere que el usuario conceda acceso a notificaciones |
| Navegación | Abre las apps de mapas instaladas (`geo:`) | No hay motor de navegación propio |
| Bluetooth | Estado real (apagado/encendido/audio conectado/permiso) | Solo lectura; manos libres según la radio |
| Teléfono | Abre el marcador del sistema y ajustes Bluetooth | No lee contactos |
| Aplicaciones | Rejilla de apps instaladas | — |
| Accesos directos | 4 ranuras configurables en Ajustes | — |
| Red | Wi-Fi / datos / sin conexión | — |
| Vehículo | **"Datos no disponibles"** (no hay interfaz verificada); simulador opcional marcado **SIMULADO** | Sin datos reales |
| Android Auto | Tarjeta de estado: **sin verificar** | No habilitable por software |
| Seguridad en conducción | `DrivingPolicy`: bloquea vídeo/escritura salvo estacionamiento confirmado | Sin velocidad real, el estado es "desconocido" |
| Diagnóstico | Ajustes → lista de hardware leída con APIs públicas + rendimiento medido | — |

## Empezar

```bash
cd DX3-OS
./gradlew -Pdx3.withAndroid=false :core:test           # pruebas de la lógica
tools/checks/check-safety.sh                           # comprobaciones de seguridad
ANDROID_HOME=~/Android/Sdk KOTLIN_HOME=~/kotlinc tools/build/build-apk.sh   # APK sin AGP
# o, con Android Studio / Google Maven:  ./gradlew :app:assembleDebug
adb install build/apk/dx3os-debug.apk                  # en una tableta de pruebas (no en la radio todavía)
```

## Documentos

- [HARDWARE_REPORT.md](HARDWARE_REPORT.md) — lo confirmado, lo desconocido y las pruebas necesarias
- [ARCHITECTURE.md](ARCHITECTURE.md) — opciones A–D, decisiones y estructura
- [SECURITY.md](SECURITY.md) — reglas de seguridad del vehículo y privacidad
- [docs/STAGE1_REPORT.md](docs/STAGE1_REPORT.md) — entorno, viabilidad, resultados y pendientes
- [docs/ANDROID_AUTO.md](docs/ANDROID_AUTO.md) · [docs/DEPENDENCIES.md](docs/DEPENDENCIES.md) ·
  [docs/DEVELOPMENT_PLAN.md](docs/DEVELOPMENT_PLAN.md) · [docs/PERFORMANCE.md](docs/PERFORMANCE.md)
- `tools/diagnostics/dx3-readonly-diag.sh` — diagnóstico ADB de solo lectura

## Licencia

Pendiente de definir por el propietario del proyecto (el repositorio contenedor usa MIT).
