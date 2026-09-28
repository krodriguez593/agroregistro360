# Design Thinking aplicado al proyecto

AgroRegistro 360 se construye siguiendo las cinco etapas del Design Thinking. Este documento explica cómo cada etapa se traduce en herramientas concretas y entregables verificables.

## Marco general

```
   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
   │             │  │             │  │             │  │             │  │             │
   │  EMPATIZAR  │─▶│   DEFINIR   │─▶│    IDEAR    │─▶│  PROTOTIPAR │─▶│   EVALUAR   │
   │             │  │             │  │             │  │             │  │             │
   └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘
         │                │                │                │                │
         ▼                ▼                ▼                ▼                ▼
   Entrevistas       Árbol de         Banco de         Prototipo        Pruebas de
   Encuesta          problemas        ideas            navegable        usabilidad
   Observación       Journey          Priorización     Interoperab.     30 indicadores
```

Cada etapa se retroalimenta: si en Evaluar encontramos que el prototipo no resuelve el problema, volvemos a Definir. El proceso es iterativo, no lineal.

## Etapa 1 — Empatizar

**Objetivo:** entender la experiencia real del productor y del funcionario, no lo que suponemos que viven.

**Herramientas aplicadas:**

- Guion de entrevista semiestructurada a productores (18 preguntas — Anexo B)
- Guion de entrevista a funcionarios del MAG (16 preguntas — Anexo C)
- Encuesta estructurada (27 preguntas — Anexo D, publicada en Google Forms)
- Ficha de observación en ventanilla (Anexo E)
- Mapa de actores y arquetipos de usuario

**Meta de muestra:** 30 encuestas + 6-10 entrevistas a productores + 3-5 entrevistas a funcionarios. Cobertura de al menos 3 provincias y presencia de 50% de pequeños productores.

**Aprendizaje clave esperado:** el problema no es la falta de un formulario en línea. Es que el Estado le pide al productor lo que ya tiene.

## Etapa 2 — Definir

**Objetivo:** convertir hallazgos en un problema accionable.

**Herramientas aplicadas:**

- Árbol de problemas con causas y efectos (Anexo M)
- Técnica de los 5 por qué para llegar a la causa raíz
- Mapa de experiencia del trámite AS-IS (Anexo F)
- Formulación del reto en formato *¿Cómo podríamos…?*

**Reto formulado:**

> *¿Cómo podríamos lograr que un pequeño productor complete su registro o renovación sin trasladarse repetidamente y conociendo en todo momento el estado de su trámite, sin perder el rigor de la verificación jurídica y técnica?*

## Etapa 3 — Idear

**Objetivo:** generar muchas ideas y elegir las que más valor entregan con el esfuerzo disponible.

**Herramientas aplicadas:**

- Sesiones de generación de ideas (brainstorming individual seguido de agrupación)
- Matriz de priorización impacto-esfuerzo-equidad (Anexo I)
- Fórmula de puntaje ponderado:
  ```
  Puntaje = Impacto productor × 0.30
          + Impacto institucional × 0.20
          + Viabilidad técnica × 0.20
          + Esfuerzo inverso × 0.15
          + Equidad × 0.15
  ```

**Ideas priorizadas (puntaje ≥ 4.0) que entran al prototipo:**

1. Formulario en línea con validación automática
2. Seguimiento del trámite en tiempo real
3. Interoperabilidad con 5 entidades del Estado
4. Firma electrónica del registro emitido
5. Notificaciones automáticas
6. Agenda digital de inspecciones
7. Expediente digital único
8. Canal asistido en asociaciones (para no dejar atrás a los sin conectividad)

## Etapa 4 — Prototipar

**Objetivo:** construir algo tangible que se pueda probar.

**Herramientas aplicadas:**

- Wireframes de baja fidelidad para los flujos críticos
- Prototipo navegable de media fidelidad (este repositorio)
- Interoperabilidad simulada con datos ficticios (`src/data/mock-db.json`)
- Cinco entidades del Estado representadas: Registro Civil, SRI, IESS, Registro de la Propiedad, Agrocalidad

**Flujos implementados:**

- Vista **productor**: nueva solicitud → interoperabilidad → carga de documentos → firma electrónica → seguimiento
- Vista **funcionario**: bandeja → revisión con datos consolidados → aprobar / observar / rechazar
- Vista **gerencial**: tablero de indicadores con comparativo antes/después

## Etapa 5 — Evaluar

**Objetivo:** verificar que la solución resuelve el problema.

**Herramientas aplicadas:**

- Protocolo de prueba de usabilidad (Anexo J) — 7 tareas medibles
- Cuestionario de satisfacción post-prueba (Anexo K) — escala 1-5
- Tablero de 30 indicadores con línea base y meta (Anexo H — Excel)
- Iteración del prototipo con hallazgos

**Muestra de la prueba:** 5-8 productores con perfiles distintos + 3-5 funcionarios. Con 5 participantes por perfil se detectan la mayoría de los problemas graves de usabilidad (regla de Nielsen).

**Criterios de éxito:**

- Tasa de éxito en tareas ≥ 80%
- Puntaje de usabilidad ≥ 70/100
- Satisfacción ≥ 4.0/5
- Tiempo de completado de la solicitud < 20 minutos

## Honestidad metodológica

En un proyecto académico no medimos el "después" en producción. Lo que hacemos:

| Se mide de verdad | Se estima con modelo | Queda propuesto |
|---|---|---|
| Usabilidad, tasa de éxito, tiempo de completado, satisfacción sobre el prototipo | Tiempo de ciclo, costo, papel evitado — con supuestos declarados | 18 indicadores para medir en el sistema real |

Presentar una proyección como resultado real invalida el trabajo. Declarar la limitación lo fortalece.

## Herramientas de referencia

- IDEO — *Design Kit for Human-Centered Design*
- Stanford d.school — *Design Thinking Bootleg*
- Nielsen Norman Group — *Usability Testing*
- Design Council UK — *Double Diamond*
