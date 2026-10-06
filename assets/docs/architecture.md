# Arquitectura del sistema — AgroRegistro 360

Este documento describe la arquitectura propuesta para AgroRegistro 360. El prototipo actual (`index.html`) implementa las capas de presentación y de simulación de interoperabilidad; el resto se documenta como referencia para una eventual implementación productiva.

## Diagrama de capas

```
┌─────────────────────────────────────────────────────────────────┐
│                      CAPA DE PRESENTACIÓN                       │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐         │
│  │ Portal Web   │   │ App Móvil    │   │ Tablero KPI  │         │
│  │ (productor)  │   │ (inspector)  │   │ (gerencia)   │         │
│  └──────┬───────┘   └──────┬───────┘   └──────┬───────┘         │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
          │                  │                  │
┌─────────▼──────────────────▼──────────────────▼─────────────────┐
│                      API GATEWAY / BFF                          │
│   Autenticación · Autorización · Rate limiting · Auditoría      │
└─────────┬───────────────────────────────────────────────────────┘
          │
┌─────────▼───────────────────────────────────────────────────────┐
│                     CAPA DE SERVICIOS                           │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐    │
│  │ Solicitud  │ │ Expediente │ │ Inspección │ │ Notificac. │    │
│  │  Service   │ │  Service   │ │  Service   │ │  Service   │    │
│  └────────────┘ └────────────┘ └────────────┘ └────────────┘    │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐    │
│  │ Interop.   │ │  Firma     │ │  Reportes  │ │  Auditoría │    │
│  │  Service   │ │ Electrón.  │ │  Service   │ │  Service   │    │
│  └─────┬──────┘ └────────────┘ └────────────┘ └────────────┘    │
└────────┼─────────────────────────────────────────────────────────┘
         │
┌────────▼─────────────────────────────────────────────────────────┐
│              CAPA DE INTEROPERABILIDAD (BUS DE DATOS)            │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐     │
│  │ Registro   │ │    SRI     │ │    IESS    │ │ Registro   │     │
│  │   Civil    │ │            │ │            │ │ Propiedad  │     │
│  └────────────┘ └────────────┘ └────────────┘ └────────────┘     │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐                    │
│  │Agrocalidad │ │ Unibanano  │ │    SIPA    │                    │
│  └────────────┘ └────────────┘ └────────────┘                    │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│               CAPA DE PERSISTENCIA Y ANALÍTICA                   │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐     │
│  │  BD OLTP   │ │Doc. Store  │ │ Data Lake  │ │ Cola Msgs. │     │
│  │(expedien.) │ │(archivos)  │ │(analítica) │ │(eventos)   │     │
│  └────────────┘ └────────────┘ └────────────┘ └────────────┘     │
└──────────────────────────────────────────────────────────────────┘
```

## Componentes del prototipo actual

El prototipo se implementó como **SPA de una sola página** (single HTML file), sin dependencias externas ni build steps. Esto asegura portabilidad total: se ejecuta desde GitHub Pages, un pen drive o cualquier servidor estático.

| Componente | Ubicación | Responsabilidad |
|---|---|---|
| Vista landing | `index.html` → `viewLanding()` | Página de inicio con KPIs y features |
| Vista nueva solicitud | `viewNuevaSolicitud()` + `renderPaso2/3/4()` | Wizard de 4 pasos con interoperabilidad |
| Vista mis trámites | `viewMisTramites()` | Tabla de expedientes del productor |
| Vista detalle | `viewDetalleTramite()` | Timeline y datos del expediente |
| Vista bandeja | `viewBandeja()` | Cola de expedientes para el funcionario |
| Vista expediente | `viewExpediente()` | Revisión, aprobación, observación |
| Vista tablero | `viewTablero()` | KPIs, gráficos SVG antes/después |
| Servicio interop. | `consultarInterop()` | Consulta secuencial a las 5 entidades |
| Base de datos | `src/data/mock-db.json` | Datos ficticios de las 5 entidades |

## Decisiones de diseño

### ¿Por qué HTML/CSS/JS sin frameworks?

1. **Portabilidad**: se ejecuta en cualquier navegador moderno sin build.
2. **Transparencia académica**: el evaluador ve el código sin capas de abstracción.
3. **Cero costo de hosting**: GitHub Pages sirve la página gratis.
4. **Foco en la lógica del servicio**, no en el stack tecnológico.

### ¿Por qué SVG inline para los gráficos?

- No requiere librerías (Chart.js pesa ~150 KB).
- Se renderiza en modo oscuro con `currentColor`.
- Auditables directamente en el DOM.

### Modo oscuro

Implementado con variables CSS y `prefers-color-scheme`. El usuario respeta la configuración del sistema.

## Recomendaciones para producción

Si el MAG decidiera implementar la solución, se recomienda:

| Aspecto | Tecnología sugerida | Motivo |
|---|---|---|
| Frontend | React o Vue + TypeScript | Escalabilidad y componentización |
| Backend | Node.js/NestJS o Python/FastAPI | Ecosistema y disponibilidad de talento local |
| BD OLTP | PostgreSQL | Robusto, gratuito, ampliamente soportado |
| Doc. store | MinIO / S3 | Almacenamiento de PDFs firmados |
| Cola de mensajes | RabbitMQ / Kafka | Notificaciones asíncronas |
| Autenticación | OAuth 2.0 + Keycloak | Integración con firma electrónica |
| Interoperabilidad | Bus con circuit breakers | Tolerancia a fallos de entidades externas |
| Infraestructura | Kubernetes en nube pública o privada | Alta disponibilidad |
| Observabilidad | Prometheus + Grafana + Loki | Métricas, logs y alertas |
| Ciberseguridad | Cumplimiento Ley 290 (2026) | Auditorías periódicas, cifrado en tránsito y reposo |

## Interoperabilidad — flujo detallado

Ver [interoperability.md](interoperability.md).

## Indicadores de gestión

Ver [indicators.md](indicators.md).
