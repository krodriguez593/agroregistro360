# 🌱 AgroRegistro 360

> Plataforma digital integrada para el registro, actualización y renovación del productor de musáceas del Ministerio de Agricultura y Ganadería del Ecuador (MAG).

[![Live Demo](https://img.shields.io/badge/demo-live-brightgreen)](https://krodriguez593.github.io/agroregistro360/)
[![Design Thinking](https://img.shields.io/badge/methodology-Design%20Thinking-5B2D90)](docs/design-thinking.md)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
[![Status](https://img.shields.io/badge/status-Prototype-orange)]()

Proyecto de innovación pública desarrollado como parte del **Diplomado en Innovación Pública 2026** — Equipo #2.

---

## 🎯 El problema

El subproceso **MAG-SFM-P03-SP01** (Registro, actualización o renovación como productor) se gestiona hoy mediante expedientes físicos y validación manual, con integración limitada entre los sistemas Unibanano y SIPA. El productor de banano y plátano debe trasladarse repetidamente, presentar en papel documentos que el Estado ya posee y esperar sin conocer el estado de su expediente. En un sector exportador clave del Ecuador, ese costo de transacción termina desincentivando la formalización de los pequeños productores.

## 💡 La solución

**AgroRegistro 360** propone digitalizar el trámite de extremo a extremo con:

- 🔗 **Interoperabilidad** con 5 entidades del Estado (Registro Civil, SRI, IESS, Registro de la Propiedad, Agrocalidad)
- 📄 **Expediente digital único** por productor y por predio
- ✍️ **Firma electrónica** con validez legal (LOTD)
- 🕐 **Seguimiento en tiempo real** del trámite
- 🔔 **Notificaciones automáticas** por correo y SMS
- 📊 **Tablero de indicadores** con comparativo antes/después
- 🗺️ **Agenda digital de inspecciones** con georreferenciación
- ♿ **Canal asistido** para mitigar la brecha digital

## 🚀 Demo en vivo

**👉 [krodriguez593.github.io/agroregistro360](https://krodriguez593.github.io/agroregistro360/)**

### Cédulas ficticias de prueba

Todos los datos son inventados para fines académicos:

| Cédula | Productor | Provincia | Perfil |
|---|---|---|---|
| `0912345678` | María Elena Cedeño | El Oro | Banano BPA, 18.5 ha |
| `0701234567` | Luis Alfredo Montaño | Los Ríos | Plátano exportación, 42 ha, hipoteca |
| `1305678901` | Rosa Margarita Zambrano | Manabí | Banano orgánico, 6.2 ha, sin IESS |

### Cómo probar

1. Abrí el enlace de la demo.
2. Click en **"Iniciar una solicitud"**.
3. Ingresá una de las cédulas de prueba y ve cómo la plataforma consulta las 5 entidades del Estado.
4. Completá el flujo (4 pasos) hasta firmar.
5. Cambiá a la vista **Funcionario MAG** (botón superior derecho) para aprobar u observar el expediente.
6. Revisá el **Tablero KPI** con la comparativa antes/después.

## 🧭 Metodología

El proyecto aplica **Design Thinking** en sus cinco etapas:

| Etapa | Herramienta principal | Entregable |
|---|---|---|
| Empatizar | Encuestas, entrevistas, observación en ventanilla | Mapa de actores, arquetipos |
| Definir | Árbol de problemas, 5 por qué, journey AS-IS | Anexos M y F |
| Idear | Matriz de priorización impacto-esfuerzo-equidad | Anexo I |
| Prototipar | HTML/CSS/JS con interoperabilidad simulada | Este repositorio |
| Evaluar | Pruebas de usabilidad + tablero de 30 indicadores | Anexos H, J, K |

## 📊 Impacto proyectado

| Indicador | Antes | Después | Δ |
|---|---|---|---|
| Tiempo del trámite | 22 días | 4 días | **-82%** |
| Visitas presenciales | 3.4 | 1.0 | **-71%** |
| Costo para el productor | USD 84 | USD 22 | **-74%** |
| Tasa de reprocesos | 38% | 9% | **-76%** |
| Satisfacción (1-5) | 2.4 | 4.3 | **+79%** |
| Trazabilidad | 0% | 100% | **+100 pp** |

*Cifras del prototipo; la línea base final se cierra con el trabajo de campo (30 encuestas + 5 entrevistas).*

## 🎯 ODS alineados

- **ODS 9** — Industria, innovación e infraestructura *(principal)*
- **ODS 16** — Paz, justicia e instituciones sólidas *(principal)*
- **ODS 2** — Hambre cero *(complementario)*
- **ODS 8** — Trabajo decente y crecimiento económico *(complementario)*
- **ODS 12** — Producción y consumo responsables *(complementario)*

## 📁 Estructura del repositorio

```
agroregistro360/
├── index.html               # Prototipo funcional (entrada de GitHub Pages)
├── src/
│   ├── data/                # Bases ficticias de interoperabilidad
│   │   └── mock-db.json
│   ├── js/                  # Módulos separados (versión modular)
│   └── css/                 # Estilos separados (versión modular)
├── docs/                    # Documentación del proyecto
│   ├── brief.md
│   ├── design-thinking.md
│   ├── architecture.md
│   ├── interoperability.md
│   └── indicators.md
├── assets/                  # Imágenes y recursos
├── .github/workflows/       # Auto-publicación en GitHub Pages
└── LICENSE
```

## 🛠️ Tecnologías

- **Frontend**: HTML5, CSS3, JavaScript (vanilla — sin frameworks para máxima portabilidad)
- **Diseño**: Sistema de diseño propio inspirado en material design, soporte modo oscuro
- **Publicación**: GitHub Pages con workflow automático
- **Datos**: JSON estático que simula APIs REST de las 5 entidades del Estado

## 👥 Equipo

| Integrante | Rol |
|---|---|
| Diego Patricio Ocampo Lascano | Vocero |
| André Esteban Chávez Raza | Mediación de conflictos |
| Kevin Isidro Rodríguez Ponce | Planificación y monitoreo |
| Karen María Párraga Bazurto | Bienestar del equipo |
| Jorge Paul Ordóñez Andrade | Control de calidad |

**Tutor:** Ing. Juan Carlos Moscoso García

## 📜 Marco normativo

- Constitución de la República del Ecuador
- Ley Orgánica para Estimular y Controlar la Producción y Comercialización de Banano, Plátano y otras Musáceas
- Decreto Ejecutivo No. 428 (2022)
- Ley Orgánica para la Optimización y Eficiencia de Trámites Administrativos
- Ley Orgánica de Transformación Digital
- Ley Orgánica de Protección de Datos Personales
- Ley Orgánica para el Fortalecimiento de la Ciberseguridad
- Acuerdos Ministeriales 030-2025, 103-2020 y 107-2020

## ⚠️ Advertencia académica

Este es un **prototipo académico**. Todos los datos, cédulas, RUCs y registros son **ficticios** y no corresponden a personas reales. Las APIs de interoperabilidad son **simuladas** con datos locales; una implementación real requeriría convenios institucionales con SRI, IESS, Registro Civil, Registro de la Propiedad y Agrocalidad, además de la infraestructura de ciberseguridad correspondiente.

## 📄 Licencia

MIT — ver [LICENSE](LICENSE).

---

*Diplomado en Innovación Pública 2026 · Equipo #2 · Guayaquil, Ecuador*
