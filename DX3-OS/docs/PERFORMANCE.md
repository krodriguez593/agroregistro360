# Rendimiento

Regla: solo se registran cifras **medidas**. Lo no medido figura como "sin medir".

## Medido (Etapa 1, en el entorno de compilación)

| Métrica | Valor | Cómo |
|---|---|---|
| Tamaño del APK (debug, R8) | 75 448 bytes | `tools/build/build-apk.sh` |

## Sin medir (requiere dispositivo)

| Métrica | Herramienta |
|---|---|
| Arranque en frío hasta primer dibujado | Ajustes → Rendimiento (también `logcat -s DX3Perf`), y `adb shell am start -W ec.dx3os.launcher/.MainActivity` |
| PSS total / heap Java / heap nativo | Ajustes → "Medir memoria ahora"; `adb shell dumpsys meminfo ec.dx3os.launcher` |
| Fluidez (cuadros lentos) | `adb shell dumpsys gfxinfo ec.dx3os.launcher` |
| CPU en reposo en Inicio | `adb shell top -n 1 | grep dx3os` |

## Medidas de diseño que buscan bajo consumo (por verificar con cifras)

- Sin librerías de UI: no hay inicialización de Compose/AppCompat.
- Reloj: un tick por minuto; sin temporizadores por segundo salvo la barra de progreso de
  Música, y solo mientras esa pantalla es visible y suena algo.
- Monitores (BT, red, media, apps) registrados solo con una pantalla visible.
- Solo existe la pantalla visible; las demás se destruyen.
- Iconos de apps en caché LRU de 48 entradas, vaciada en `onTrimMemory`.
- Sin servicios en primer plano ni tareas en segundo plano.

## Plantilla para resultados en el hardware real

| Fecha | Dispositivo | Android | Arranque frío (ms) | PSS (MB) | Cuadros lentos (%) | Notas |
|---|---|---|---|---|---|---|
| | | | | | | |
