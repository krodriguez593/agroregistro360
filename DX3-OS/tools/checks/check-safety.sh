#!/usr/bin/env bash
# Comprobaciones estáticas de seguridad del proyecto DX3 OS.
#  1. El script de diagnóstico solo contiene comandos de lectura.
#  2. El código de la app no abre interfaces del vehículo (CAN, puertos serie, MCU) ni pide permisos peligrosos.
# Salida distinta de 0 si algo falla.
set -uo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
fail=0

DIAG="$ROOT/tools/diagnostics/dx3-readonly-diag.sh"
# Solo se analizan las líneas que ejecutan algo en el dispositivo (run ... / adb ...), no los comentarios.
# Se descarta la redirección de errores a /dev/null (lectura segura); cualquier otra redirección falla.
CMDS="$(grep -E '^\s*(run |adb )' "$DIAG" | grep -vE '^\s*#' | sed 's#2>/dev/null##g')"
FORBIDDEN='(\bsetprop\b|\breboot\b|\bfastboot\b|\bflash|\bdd\b|\brm\b|\bmv\b|\bcp\b|\bchmod\b|\bchown\b|\bmkfs|\bwipe|\bformat\b|\binstall\b|\buninstall\b|\bpush\b|\bsu\b|\boem\b|\bunlock|\bsideload\b|\bam (start|broadcast|force-stop)|\bpm (clear|disable|enable|grant|revoke|hide)|\bsettings put\b|\bsvc\b|\bsendevent\b|\binput (tap|keyevent|swipe|text)|\becho [^|]*>|> ?/)'
if echo "$CMDS" | grep -nE "$FORBIDDEN"; then
  echo "FALLO: el script de diagnóstico contiene comandos no permitidos (arriba)." >&2
  fail=1
else
  echo "OK  diagnóstico: $(echo "$CMDS" | grep -c '^\s*run ') comandos, todos de solo lectura"
fi

SRC="$ROOT/app/src/main"
if grep -rnE '/dev/(tty|can|spidev|i2c)|SocketCAN|can0|UsbSerial|SerialPort' "$SRC"; then
  echo "FALLO: la app referencia interfaces de bajo nivel del vehículo." >&2
  fail=1
else
  echo "OK  app: sin acceso a CAN / serie / MCU"
fi

PERMS="$(grep -oE 'android.permission\.[A-Z_]+' "$SRC/AndroidManifest.xml" | sort -u)"
ALLOWED='^android.permission.(ACCESS_NETWORK_STATE|BLUETOOTH|BLUETOOTH_CONNECT|BIND_NOTIFICATION_LISTENER_SERVICE)$'
BAD="$(echo "$PERMS" | grep -vE "$ALLOWED" || true)"
if [[ -n "$BAD" ]]; then
  echo "FALLO: permisos no aprobados en el manifiesto:" >&2; echo "$BAD" >&2; fail=1
else
  echo "OK  manifiesto: $(echo "$PERMS" | wc -l | tr -d ' ') permisos, todos en la lista aprobada (sin INTERNET)"
fi

exit $fail
