# Tablero de indicadores

AgroRegistro 360 mide su impacto con **30 indicadores** organizados en seis dimensiones. Cada uno tiene línea base (medición del "antes") y meta (proyección del "después"). El detalle completo con fórmulas y fuentes está en el **Anexo H** de los soportes metodológicos (`AgroRegistro360_Indicadores.xlsx`).

## Resumen ejecutivo

| Dimensión | Nº de indicadores | Indicador estrella |
|---|---|---|
| Eficiencia | 7 | Tiempo de ciclo del trámite: **22 → 4 días** |
| Eficacia | 5 | Trámites en plazo: **62% → 90%** |
| Igualdad y equidad | 6 | Brecha pequeños vs grandes: **12 → 3 días** |
| Calidad y experiencia | 6 | Satisfacción usuario: **2.4 → 4.3** |
| Transparencia | 4 | Trámites consultables en línea: **0% → 100%** |
| Sostenibilidad | 2 | Hojas de papel evitadas al año: **~120.000** |

## Diseño de la medición

El equipo distingue explícitamente qué se **mide**, qué se **estima** y qué queda **propuesto** para una implementación productiva. Presentar una proyección como resultado real invalida el trabajo académico.

| Momento | Qué se mide | Con qué instrumento | Cuándo |
|---|---|---|---|
| Antes (línea base) | 24 de los 30 indicadores | Encuesta, entrevistas, observación, registros | Antes de idear la solución |
| Durante (formativa) | Hallazgos de usabilidad, errores | Prueba de usabilidad | En cada iteración del prototipo |
| Después (medición real) | 6 indicadores sobre el prototipo | Pruebas de usabilidad y cuestionario | Al cerrar la etapa Evaluar |
| Después (estimado) | 6 indicadores con modelo | Estimación con supuestos declarados | Informe final |
| Propuesto | 18 indicadores para implementación | Tablero real del sistema | Fuera del alcance académico |

## Detalle por dimensión

### Eficiencia (7 indicadores)

Miden si se logra lo mismo con menos tiempo, dinero y esfuerzo.

- **EF-01** Tiempo de ciclo del trámite (días)
- **EF-02** Traslados presenciales por trámite
- **EF-03** Costo de transacción para el productor (USD)
- **EF-04** Tasa de reprocesos o devoluciones (%)
- **EF-05** Documentos que debe conseguir el usuario
- **EF-06** Consumo de papel por expediente (hojas)
- **EF-07** Horas de funcionario por expediente

### Eficacia (5 indicadores)

Miden si se cumple el objetivo del subproceso.

- **EA-01** Trámites resueltos dentro del plazo (%)
- **EA-02** Solicitudes completadas sin abandono (%)
- **EA-03** Expedientes con trazabilidad completa (%)
- **EA-04** Días hasta la inspección del predio
- **EA-05** Registros vigentes y actualizados (%)

### Igualdad y equidad (6 indicadores)

Un servicio digital puede mejorar el promedio y empeorar la situación de los más vulnerables. Estos indicadores se miden **desagregados**.

- **IG-01** Brecha de tiempo: predios <30 ha vs >30 ha (días)
- **IG-02** Participación de pequeños productores (%)
- **IG-03** Titularidad femenina del registro (%)
- **IG-04** Usuarios con 2+ visitas y más de 2h de viaje (%)
- **IG-05** Cobertura del canal asistido (%)
- **IG-06** Usuarios sin conectividad o sin teléfono (%)

### Calidad y experiencia (6 indicadores)

Miden si la persona entiende, puede y queda conforme.

- **CA-01** Satisfacción del usuario externo (escala 1-5)
- **CA-02** Claridad percibida de los requisitos (1-5)
- **CA-03** Tasa de éxito en tareas del prototipo (%)
- **CA-04** Puntaje de usabilidad (0-100)
- **CA-05** Tiempo de completado de la solicitud (min)
- **CA-06** Satisfacción del usuario interno (1-5)

### Transparencia (4 indicadores)

Miden si se puede saber qué pasó, cuándo y quién lo hizo.

- **TR-01** Trámites con estado consultable en línea (%)
- **TR-02** Documentos obtenidos por interoperabilidad (%)
- **TR-03** Actuaciones notificadas automáticamente (%)
- **TR-04** Reclamos por cada 100 trámites

### Sostenibilidad (2 indicadores)

- **TR-05** Hojas de papel evitadas por año
- **TR-06** Kilómetros de traslado evitados por año

## Cómo se ven en el prototipo

El tablero implementado en `index.html` (vista `viewTablero()`) muestra:

1. **KPIs comparativos** con delta antes/después.
2. **Gráfico de barras** del tiempo de trámite (antes / meta / después).
3. **Gráfico de dona** de interoperabilidad efectiva.
4. **Barras de progreso** de cumplimiento por criterio.
5. **Gráfico de línea** de solicitudes procesadas por semana.

Todos los gráficos son SVG inline, sin librerías externas.
