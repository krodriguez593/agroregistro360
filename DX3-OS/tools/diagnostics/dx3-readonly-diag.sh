#!/usr/bin/env bash
# DX3 OS — diagnóstico de SOLO LECTURA de la radio por ADB.
#
# QUÉ HACE: ejecuta únicamente comandos de consulta (getprop, cat de /proc y /sys, dumpsys,
#           pm list, wm size, getevent -p) y guarda la salida en una carpeta local.
# QUÉ NO HACE: no escribe en el equipo, no instala, no reinicia, no cambia propiedades,
#           no usa root, no desbloquea, no flashea, no lee eventos táctiles en vivo.
#
# Privacidad: el número de serie, IMEI y direcciones MAC se REDACTAN en la salida.
# La lista de paquetes solo se filtra por palabras clave técnicas (carplay, auto, bt, can…).
#
# Uso:
#   tools/diagnostics/dx3-readonly-diag.sh            # usa el único dispositivo conectado
#   ANDROID_SERIAL=XXXX tools/diagnostics/dx3-readonly-diag.sh
#
# Requisitos: adb (platform-tools) y la depuración USB/Wi-Fi activada en la radio
# (normalmente en un menú de fábrica; NO introduzca códigos de menú de fábrica que no conozca:
#  algunos menús permiten borrar o actualizar el sistema).
set -uo pipefail

command -v adb >/dev/null || { echo "adb no encontrado. Instale Android platform-tools." >&2; exit 1; }

STATE="$(adb get-state 2>/dev/null || true)"
if [[ "$STATE" != "device" ]]; then
  echo "No hay un dispositivo ADB autorizado (estado: '${STATE:-ninguno}')." >&2
  echo "Ejecute 'adb devices' y acepte la huella RSA en la pantalla de la radio." >&2
  exit 2
fi

OUT="diagnostics-output/dx3-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$OUT"
echo "Guardando en $OUT"

redact() {
  sed -E \
    -e 's/(ro\.(boot\.)?serialno\]: \[)[^]]*/\1REDACTADO/g' \
    -e 's/(ro\.(boot\.)?(imei|meid|mac[a-z_]*|wifimac|btmac)[^]]*\]: \[)[^]]*/\1REDACTADO/Ig' \
    -e 's/([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}/XX:XX:XX:XX:XX:XX/g' \
    -e 's/(Serial[[:space:]]*:[[:space:]]*)[0-9A-Fa-f]+/\1REDACTADO/g'
}

# run <archivo> <comando de shell remoto de solo lectura>
run() {
  local file="$1"; shift
  printf '$ adb shell %s\n' "$*" > "$OUT/$file"
  adb shell "$@" 2>&1 | redact >> "$OUT/$file"
  printf '  %-28s %s\n' "$file" "ok"
}

# 1. Identificación del sistema
run 01-getprop.txt getprop
run 02-uname.txt uname -a
run 03-build-prop.txt cat /system/build.prop
run 04-vendor-build-prop.txt cat /vendor/build.prop

# 2. Procesador, memoria, almacenamiento
run 10-cpuinfo.txt cat /proc/cpuinfo
run 11-meminfo.txt cat /proc/meminfo
run 12-df.txt df -h
run 13-partitions.txt cat /proc/partitions
run 14-mounts.txt cat /proc/mounts
run 15-cpufreq.txt 'cat /sys/devices/system/cpu/cpu*/cpufreq/scaling_available_frequencies 2>/dev/null; cat /sys/devices/system/cpu/cpu*/cpufreq/cpuinfo_max_freq 2>/dev/null'

# 3. Pantalla y tacto (getevent -p SOLO lista dispositivos; no captura eventos)
run 20-wm-size.txt wm size
run 21-wm-density.txt wm density
run 22-display.txt dumpsys display
run 23-input-devices.txt getevent -p
run 24-dumpsys-input.txt dumpsys input

# 4. Funciones, controladores y módulos
run 30-features.txt pm list features
run 31-modules.txt cat /proc/modules
run 32-dev-nodes.txt 'ls -l /dev/video* /dev/tty* /dev/snd 2>/dev/null'
run 33-usb.txt 'ls /sys/bus/usb/devices 2>/dev/null; cat /sys/bus/usb/devices/*/product 2>/dev/null'
run 34-hal-services.txt 'lshal 2>/dev/null || service list'

# 5. Bluetooth, audio, cámara, multimedia
run 40-bluetooth.txt dumpsys bluetooth_manager
run 41-audio.txt dumpsys audio
run 42-camera.txt dumpsys media.camera
run 43-media-session.txt dumpsys media_session

# 6. Paquetes relevantes (filtrados: no se listan apps personales)
run 50-packages-relevant.txt 'pm list packages -f | grep -iE "carplay|androidauto|gearhead|projection|zlink|autokit|carlink|carbit|easyconnect|bluetooth|bt|canbus|can\.|mcu|radio|launcher|camera|reverse|backcar|settings|update|ota|factory"'
run 51-launchers.txt 'cmd package query-activities -a android.intent.action.MAIN -c android.intent.category.HOME 2>/dev/null || pm query-activities -a android.intent.action.MAIN -c android.intent.category.HOME'

# 7. Arranque y actualización (lectura de propiedades; NO se toca el bootloader)
run 60-boot-props.txt 'getprop | grep -iE "ro\.boot\.|bootloader|verifiedboot|ro\.secure|ro\.debuggable|slot_suffix|ota|recovery"'

echo
echo "Listo. Revise $OUT y adjunte la carpeta al informe (ya se redactaron serie/IMEI/MAC)."
echo "Nada se modificó en la radio."
