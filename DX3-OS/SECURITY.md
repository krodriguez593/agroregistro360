# SECURITY — Seguridad del automóvil y del usuario

La seguridad tiene prioridad sobre la personalización.

## Reglas del proyecto (obligatorias)

1. **Sin interferencia con sistemas críticos.** DX3 OS no se comunica con ECU, bus CAN, airbags,
   ABS, dirección, frenos ni ningún módulo del vehículo. No abre puertos serie, `/dev/can*`,
   SPI, I2C ni interfaces de la MCU. `tools/checks/check-safety.sh` lo verifica en el código.
2. **Funciones originales intactas.** La cámara de reversa, la radio, el manos libres y los
   avisos del fabricante siguen a cargo del sistema original. DX3 OS es una app que se puede
   quitar como pantalla de inicio en cualquier momento (Ajustes → Elegir pantalla de inicio).
3. **Sin contenido distractor.** No hay reproductor de vídeo. `DrivingPolicy` bloquea vídeo y
   escritura salvo estacionamiento **confirmado**; un estado desconocido se trata como "en
   movimiento" para esas funciones. Música, navegación y teléfono nunca se bloquean.
4. **Sin datos inventados.** Los datos del vehículo muestran su procedencia; los simulados se
   marcan "SIMULADO" y están desactivados por defecto.
5. **Nada destructivo ni automático.** Ningún script del proyecto flashea, desbloquea el
   bootloader, borra particiones, instala en el dispositivo ni reinicia. El diagnóstico es de
   solo lectura y se comprueba automáticamente.
6. **Firmware.** No se sustituirá firmware sin: modelo exacto confirmado, copia de seguridad
   completa y verificada (con sumas de comprobación), procedimiento de recuperación probado y
   aprobación explícita del propietario.

## Privacidad

- Sin permiso `INTERNET`: la app no puede enviar datos a ningún servidor. Sin telemetría.
- No lee contactos, registro de llamadas, SMS, ubicación ni el contenido de notificaciones
  (el servicio de notificaciones existe solo porque Android lo exige para ver la sesión
  multimedia activa).
- El diagnóstico no lee número de serie, IMEI ni MAC; el script ADB los redacta.
- Los registros (`logcat`, etiqueta `DX3Perf`) contienen solo cifras de rendimiento.
- `allowBackup="false"`: las preferencias no se copian a la nube.

## Recuperación

Para la opción A (launcher) la recuperación es trivial: elegir otro launcher en Ajustes de
Android o desinstalar la app (`adb uninstall ec.dx3os.launcher`, o desde Ajustes → Apps).
Si la radio arrancara mostrando DX3 OS y este fallara, el botón Inicio permite elegir el
launcher original cuando Android lo pregunta, y "Ajustes de Android" está siempre accesible
desde la pantalla de Ajustes de DX3 OS.

## Reporte de problemas

Cualquier comportamiento que afecte a la conducción o a funciones originales del vehículo se
trata como error crítico: se documenta y se corrige antes de cualquier otra tarea.
