# ARCHITECTURE — DX3 OS

## 1. Evaluación de opciones (en el orden pedido)

| Opción | Viabilidad hoy | Riesgo | Decisión |
|---|---|---|---|
| **A. Launcher personalizado sobre el Android existente** | Alta **si** la radio es Android y permite instalar APK (H1/H3/H4) | Bajo: es una app normal, se desinstala; no toca firmware | ✅ **Recomendada** |
| B. Apps + servicios sobre un Android compatible | Media: requiere conocer qué servicios del fabricante (MCU, BT, radio) existen y si exponen interfaces documentadas | Bajo-medio | Evolución natural de A, tras el diagnóstico |
| C. ROM AOSP personalizada | **No viable sin** kernel, device tree, HAL y drivers del fabricante (ninguno disponible) | Alto: puede inutilizar la radio y la cámara de reversa | ❌ Descartada hasta tener código y recuperación verificados |
| D. Linux embebido | Igual que C y además habría que reescribir audio, cámara, BT y teclas | Muy alto | ❌ Descartada |

Mientras el hardware no esté identificado se desarrolla un **prototipo funcional para Android
estándar** (cualquier tableta/teléfono Android 5.0+ en horizontal) sin modificar ningún firmware.

## 2. Decisiones técnicas (ADR resumidas)

**ADR-1 · Vistas nativas del framework, sin AndroidX ni Compose.**
Las radios Android de gama baja suelen tener 1–2 GB de RAM y CPUs lentas. Usar solo el
framework da un APK de ~75 KB (medido), sin librerías que inicializar y compatible con Android
5.0 (API 21). Además, en el entorno de desarrollo de la Etapa 1 Google Maven no era accesible
(ver docs/STAGE1_REPORT.md), y esta elección permite compilar con solo el SDK.
Coste: más código de UI manual; sin las comodidades de Compose. Revisable en Etapa 5 con
mediciones reales.

**ADR-2 · Lógica en `:core` (Kotlin puro).**
Todo lo que no necesita Android (formatos, estados, política de conducción, Android Auto,
accesos directos, lista de hardware) vive en `:core` y se prueba en la JVM.

**ADR-3 · Observables con ciclo de vida, sin procesos persistentes.**
Cada monitor (`Observable`) registra receptores/callbacks solo mientras una pantalla visible lo
observa y los libera al ocultarse. No hay `Service` en primer plano, ni `WorkManager`, ni alarmas.
El reloj se actualiza una vez por minuto, no por segundo.

**ADR-4 · Datos del vehículo solo de lectura y con procedencia.**
`VehicleDataSource` no tiene operaciones de escritura. Cada dato lleva `DataProvenance`
(VERIFIED / SIMULATED / UNAVAILABLE). Hoy solo existen `UnavailableVehicleDataSource` (por
defecto) y `SimulatedVehicleDataSource` (activable a mano, etiquetado "SIMULADO").

**ADR-5 · Android Auto: detectar y documentar, nunca imitar.** Ver docs/ANDROID_AUTO.md.

**ADR-6 · Doble vía de compilación.** `app/build.gradle.kts` (AGP, estándar) y
`tools/build/build-apk.sh` (solo SDK). Compilan las mismas fuentes.

## 3. Estructura

```
DX3-OS/
├── core/                       Kotlin puro (JVM) + pruebas unitarias
│   └── ec/dx3os/core/
│       ├── clock/              formato de reloj/fecha en español
│       ├── connectivity/       estados Bluetooth y red → texto/gravedad
│       ├── media/              estado del reproductor y formatos
│       ├── vehicle/            fuente de datos (no disponible / simulada)
│       ├── safety/             política de distracción en conducción
│       ├── projection/         estado honesto de Android Auto
│       ├── launcher/           accesos directos configurables
│       └── diagnostics/        lista de verificación de hardware
├── app/                        launcher Android (framework, sin AndroidX)
│   └── ec/dx3os/launcher/
│       ├── data/               monitores: reloj, BT, red, media, apps, vehículo, perf
│       ├── ui/                 sistema visual, contrato Screen/Host
│       └── screens/            Inicio, Navegación, Música, Teléfono, Aplicaciones, Ajustes
├── tools/
│   ├── build/build-apk.sh      compilación con solo el SDK
│   ├── diagnostics/            diagnóstico ADB de solo lectura
│   └── checks/check-safety.sh  comprobaciones estáticas de seguridad
└── docs/
```

No se crearon módulos vacíos (`media/`, `connectivity/`, `vehicle-integration/` de la
estructura sugerida existen como paquetes con código real dentro de `core` y `app`). Se
separarán en módulos Gradle cuando su tamaño lo justifique.

## 4. Flujo de datos

```
Android APIs (BluetoothAdapter, ConnectivityManager, MediaSessionManager, PackageManager)
        │  (solo lectura, solo mientras hay observadores)
        ▼
app/data/*Monitor  ──►  core (estados + presentadores puros)  ──►  app/screens (vistas)
                                       ▲
VehicleDataSource (Unavailable | Simulated) ── DrivingPolicy ──► restricciones de UI
```

## 5. Si algún día se evalúa una ROM (opción C)

Capas a separar: interfaz (este launcher) · servicios del sistema · HAL (audio, cámara,
vehículo/VHAL) · kernel + device tree · drivers/firmware de periféricos. Requisitos previos
innegociables: fuentes del kernel y device tree del fabricante, copia de seguridad completa
verificada (todas las particiones), método de recuperación probado (p. ej. modo de descarga del
SoC) y confirmación del modelo exacto. Ver SECURITY.md.
