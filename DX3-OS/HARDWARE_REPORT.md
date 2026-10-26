# HARDWARE_REPORT — Radio del Soueast DX3 2020

> Estado: **Etapa 1 — hardware NO identificado.** Ningún dato de este informe procede de
> la radio real todavía. No se ha conectado ningún dispositivo, no se ha flasheado,
> desbloqueado ni borrado nada.

Leyenda: ✅ confirmado · ❔ desconocido · 🔧 requiere prueba física · 📰 fuente pública sin verificar

## 1. Lo que sabemos (fuentes públicas, sin verificar en el vehículo)

| Dato | Valor | Fuente | Estado |
|---|---|---|---|
| Vehículo | Soueast DX3, 1.5 L gasolina, tracción delantera; versiones manual y automática | Listados de Patiotuerca Ecuador y Encuentra24 | 📰 |
| Pantalla | 9 pulgadas táctil, Bluetooth y Wi-Fi; mapas de Google, GPS, radio y comandos de voz preinstalados | Una sola reseña (Elyex, 2020), no es ficha del fabricante | 📰 |
| Cámara de reversa | Aparece en el equipamiento de un DX3 **2023** publicado en Ecuador | Listado de Patiotuerca | 📰 (otro año) |
| Android Auto / CarPlay | **Ninguna fuente encontrada lo confirma** | — | ❔ |

Fuentes consultadas:
- https://elyex.com/soueast-dx3-2020-ecuador-opiniones-criticas-precio-y-ficha-tecnica/
- https://ecuador.patiotuerca.com/precio/autos/soueast/dx3
- https://ecuador.patiotuerca.com/vehicle/autos-soueast-dx3-cuenca-2023/1962722
- https://www.encuentra24.com/costa-rica-en/cars-auto-trucks-used-car/soueast-dx3-2020-automatico-poco-kilometraje-precio-negociable/31949722

Que la reseña mencione "Google Maps preinstalado" **sugiere** que la unidad corre Android,
pero no lo demuestra: podría ser otra plataforma o una unidad de reemplazo instalada por el
concesionario. **No se debe suponer que todas las radios DX3 2020 son iguales**: varían según
el mercado, la versión del vehículo, el lote de producción y si el dueño anterior cambió la radio.

## 2. Configuraciones posibles (hipótesis a descartar)

| ID | Hipótesis | Si se confirma… |
|---|---|---|
| H1 | Radio de fábrica con **Android** (proveedor desconocido) y un microcontrolador (MCU) aparte para teclas, CAN, cámara | Opción A viable (launcher) si permite instalar APK |
| H2 | Radio de fábrica con **Linux / WinCE / RTOS propietario** sin instalación de apps | Opción A imposible; solo cabe sustituir la unidad |
| H3 | Radio **de reemplazo (aftermarket) Android** genérica (frecuente en el mercado) | Opción A viable; Android Auto solo si la unidad trae receptor licenciado |
| H4 | Radio Android con versión muy antigua (4.4–6.0) y poca RAM (≤1 GB) | Opción A viable con la app ligera actual (minSdk 21 = Android 5.0); 4.4 **no** soportado |

En radios Android del mercado aparecen con frecuencia SoC como Allwinner T3/T8, Rockchip
PX30/PX5/PX6, MediaTek MT8163 o Unisoc UIS7862 y pantallas de 1024×600 o 1280×720. **Son
ejemplos del mercado, no datos del DX3.** La interfaz se diseñó para adaptarse a ambos tamaños.

## 3. Lista de identificación

| Dato necesario | Estado | Cómo verificar (solo lectura) |
|---|---|---|
| Fabricante y modelo de la unidad | ❔ | Pantalla "Información del sistema"/"Acerca de" de la radio; `getprop ro.product.manufacturer/model`; etiqueta del chasis (solo si se desmonta con seguridad) |
| Procesador y arquitectura | ❔ | `cat /proc/cpuinfo`, `getprop ro.product.cpu.abilist`, `getprop ro.board.platform` |
| RAM | ❔ | `cat /proc/meminfo` |
| Almacenamiento | ❔ | `df -h`, `cat /proc/partitions` |
| Resolución | ❔ | `wm size`, `wm density` |
| Tecnología táctil | ❔ | `getevent -p` (lista dispositivos; no captura toques), `dumpsys input` |
| Sistema operativo y versión | ❔ | `getprop ro.build.version.release`, `ro.build.fingerprint` |
| Parche de seguridad | ❔ | `getprop ro.build.version.security_patch` |
| Bootloader / verified boot | 🔧 | `getprop | grep ro.boot.` — **no** desbloquear |
| Método de actualización y firmware oficial | 🔧 | Menú de actualización de la radio, concesionario Soueast, paquetes OTA publicados por el fabricante |
| Cámara de reversa | 🔧 | Con el vehículo detenido y freno de mano, engranar reversa y observar si la radio original conmuta sola; `dumpsys media.camera`, `ls /dev/video*` |
| Mandos del volante | 🔧 | Pulsar cada botón mientras `adb shell getevent -l` registra (lectura) qué dispositivo de entrada lo emite |
| Bluetooth manos libres | 🔧 | ¿Lo gestiona Android (`dumpsys bluetooth_manager`) o un módulo propio con su app? |
| Android Auto / CarPlay de fábrica | 🔧 | Conectar teléfono con cable de datos; revisar menú y manual del vehículo; `pm list packages` filtrado (gearhead, carplay, zlink…) |
| Interfaz de datos del vehículo (velocidad, marcha) | ❔ | Paquetes "canbus"/"mcu" del fabricante — **solo para identificarlos; no se usarán sin documentación oficial** |

## 4. Controladores y firmware

| Componente | ¿Controlador disponible para nuestra app? | Comentario |
|---|---|---|
| Pantalla/táctil | Sí, mediante el Android existente | Una ROM nueva necesitaría el driver exacto del panel y del táctil: ❔ |
| Wi-Fi / Bluetooth | Sí, API pública de Android | En muchas radios el BT de manos libres es un módulo aparte con app propia: ❔ |
| Audio / radio FM-AM | No hay API pública estándar | Lo controla la app/MCU del fabricante; DX3 OS la abre, no la reemplaza |
| Cámara de reversa | No (la gestiona la MCU o la app del fabricante) | Se mantiene la función original intacta |
| Mandos del volante | Solo si llegan como `KeyEvent` estándar (MEDIA_NEXT, etc.) | Si van por MCU propietaria: requiere documentación del fabricante |
| CAN / datos del vehículo | No | Fuera de alcance por seguridad (ver SECURITY.md) |
| Kernel, device tree, HAL | Desconocido | Imprescindibles para una ROM (opción C). Sin código fuente del fabricante, la opción C no es viable |

## 5. Procedimiento de diagnóstico seguro

1. Comprobar en la radio si existe "Opciones de desarrollador" / depuración USB. Si solo se
   accede con códigos de "menú de fábrica", **no probar códigos desconocidos**: algunos menús
   permiten borrar o actualizar el sistema.
2. Conectar por USB (o ADB por Wi-Fi si la radio lo ofrece) y ejecutar
   `tools/diagnostics/dx3-readonly-diag.sh`. El script solo lee, redacta serie/IMEI/MAC y
   guarda todo en `diagnostics-output/` (ignorado por git).
3. Alternativa sin ADB: instalar el APK de DX3 OS (si la radio permite instalar apps) y abrir
   **Ajustes → Diagnóstico de hardware**; "Copiar informe" genera el mismo resumen.
4. Pruebas físicas (cámara, mandos, manos libres) siempre con el vehículo **detenido**, freno de
   mano y motor en condiciones seguras.

`tools/checks/check-safety.sh` verifica automáticamente que el script de diagnóstico no
contenga comandos de escritura (setprop, reboot, flash, dd, rm, install, input…).

## 6. Información que necesito del propietario

1. Foto de la pantalla "Información del sistema" / "Acerca de" de la radio.
2. ¿La radio es de fábrica o fue cambiada? ¿Trae tienda de apps o permite instalar APK?
3. Versión del vehículo (p. ej. Confort / Luxury) y si es manual o automático.
4. ¿El vehículo tiene cámara de reversa y mandos en el volante? ¿Funcionan hoy?
5. ¿La radio muestra alguna opción de Android Auto, CarPlay o "enlace de teléfono"?
6. Si es posible, la salida de `dx3-readonly-diag.sh`.

Hasta tener esto, **no se tomará ninguna decisión irreversible**.
