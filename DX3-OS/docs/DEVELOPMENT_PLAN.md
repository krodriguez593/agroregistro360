# Plan de desarrollo

Cada etapa termina con: archivos modificados explicados, pruebas ejecutadas, errores pendientes
y decisiones documentadas. Nada se declara terminado si es solo una maqueta. Nunca se flashea
automáticamente.

| Etapa | Objetivo | Criterio de salida | Estado |
|---|---|---|---|
| 1 | Entorno, viabilidad, diagnóstico, prototipo | Informe + APK compilado + pruebas de `core` | ✅ Hecho (salvo ejecución en dispositivo) |
| 2 | Identificar la radio y fijar arquitectura | HARDWARE_REPORT con datos ✅ de la radio real; decisión A/B confirmada | ⏳ Necesita datos del propietario |
| 3 | Prototipo visual en el dispositivo real | Capturas en la resolución real; ajustes de diseño | ⏳ |
| 4 | Navegación, multimedia, ajustes completos | Controles verificados con apps reales; mandos del volante si llegan como KeyEvent estándar | ⏳ Base implementada |
| 5 | Memoria y rendimiento | Tabla de mediciones en docs/PERFORMANCE.md (arranque, PSS, fluidez) en el hardware real | ⏳ Instrumentación lista |
| 6 | Compatibilidad y corrección de errores | Pruebas de UI automatizadas + checklist en el vehículo (detenido) | ⏳ |
| 7 | Instalación | Solo con hardware y recuperación verificados. Para la opción A: instalar APK + elegir launcher; reversible | ⏳ |

## Próximos pasos concretos

1. Propietario: enviar la información de HARDWARE_REPORT.md §6.
2. Probar el APK en una tableta Android en horizontal (`adb install build/apk/dx3os-debug.apk`).
3. Si la radio es Android: ejecutar el diagnóstico de solo lectura y actualizar el informe.
4. Configurar CI (GitHub Actions) con AGP para `:app:assembleDebug`, lint y pruebas de UI.
