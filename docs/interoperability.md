# Interoperabilidad institucional

AgroRegistro 360 elimina la solicitud de documentos que el Estado ya tiene. En lugar de que el productor cargue en papel su cédula, RUC, título de propiedad y certificación fitosanitaria, la plataforma los consulta automáticamente en tiempo real a las cinco entidades responsables.

## Entidades interoperables

| Entidad | Datos que aporta | Estado en el prototipo |
|---|---|---|
| **Registro Civil** | Identidad, nombres, fecha de nacimiento, estado civil, nacionalidad | ✅ Simulado |
| **SRI** (Servicio de Rentas Internas) | RUC, razón social, actividad económica, estado tributario, obligación de contabilidad | ✅ Simulado |
| **IESS** (Instituto Ecuatoriano de Seguridad Social) | Afiliación, tipo, empleador, última aportación | ✅ Simulado |
| **Registro de la Propiedad** | Clave catastral, ubicación, superficie, titular, tenencia, gravámenes, linderos | ✅ Simulado |
| **Agrocalidad** | Certificaciones fitosanitarias, planes fitosanitarios, vigencias | ✅ Simulado |

## Flujo de consulta

```
Productor ingresa cédula
         │
         ▼
┌─────────────────────┐
│ AgroRegistro 360    │
│ (Bus de interop.)   │
└─────────┬───────────┘
          │
    ┌─────┼─────┬─────┬─────┐
    ▼     ▼     ▼     ▼     ▼
   [RC] [SRI] [IESS] [RP] [Agrocal.]
    │     │     │     │     │
    │     │     │     │     │
    └─────┴─────┴─────┴─────┘
          │
          ▼
   Datos consolidados y
   presentados al productor
```

En el prototipo, la función `consultarInterop(cedula)` realiza estas consultas de forma **secuencial** con delays simulados de 400–1000 ms cada una, para hacer visible al usuario el proceso de verificación. En producción, las consultas serían **paralelas** con circuit breakers para tolerar la caída de una entidad sin bloquear el trámite.

## Contratos de datos (JSON Schema)

### Registro Civil

```json
{
  "cedula": "string(10)",
  "nombres": "string",
  "apellidos": "string",
  "fechaNacimiento": "YYYY-MM-DD",
  "estadoCivil": "Soltera|Casada|Divorciada|Viuda|Unión libre",
  "nacionalidad": "string",
  "estado": "VIGENTE|FALLECIDA|CANCELADA"
}
```

### SRI

```json
{
  "ruc": "string(13)",
  "razonSocial": "string",
  "tipo": "Persona Natural|Sociedad",
  "actividadEconomica": "string",
  "estado": "ACTIVO|SUSPENDIDO|PASIVO",
  "direccionTributaria": "string",
  "obligadaContabilidad": "SI|NO",
  "fechaInicio": "YYYY-MM-DD"
}
```

### IESS

```json
{
  "cedula": "string(10)",
  "afiliada": "boolean",
  "estado": "ACTIVO|CESANTE|SIN AFILIACIÓN",
  "tipoAfiliacion": "Empleador|Trabajador Autónomo|Empleado|Voluntario",
  "empleador": "string",
  "ultimaAportacion": "YYYY-MM-DD"
}
```

### Registro de la Propiedad

```json
{
  "claveCatastral": "string",
  "ubicacion": "string",
  "superficie": "number",
  "unidad": "ha|m2",
  "propietario": "string(10) — cédula del titular",
  "tipoTenencia": "PROPIETARIO|ARRENDATARIO|COMODATARIO|USUFRUCTO",
  "linderos": "string",
  "gravamenes": "string"
}
```

### Agrocalidad

```json
{
  "cedula": "string(10)",
  "tieneRegistro": "boolean",
  "tipoCertificacion": "BPA|Global GAP|Producción Orgánica|Otra",
  "fechaVigencia": "YYYY-MM-DD",
  "planFitosanitario": "string",
  "estado": "VIGENTE|VENCIDO|SUSPENDIDO"
}
```

## Consideraciones de ciberseguridad

Para una implementación productiva, aplican:

1. **Autenticación mutua** con certificados X.509 entre servidores.
2. **Cifrado en tránsito** (TLS 1.3) para todas las llamadas.
3. **Consentimiento del titular** registrado en cada consulta, conforme a la Ley Orgánica de Protección de Datos Personales.
4. **Registro de auditoría** inmutable de qué se consultó, cuándo y por quién.
5. **Minimización de datos**: solicitar solo los campos estrictamente necesarios para el trámite.
6. **Circuit breakers y timeouts** para no bloquear el trámite si una entidad no responde.
7. **Cifrado en reposo** de los datos personales.
8. **Cumplimiento del Reglamento a la Ley 290 de 2026** (fortalecimiento de la ciberseguridad).

## Marco legal habilitante

- Ley Orgánica para la Optimización y Eficiencia de Trámites Administrativos (2018)
- Ley Orgánica de Transformación Digital
- Ley Orgánica de Protección de Datos Personales
- Ley Orgánica para el Fortalecimiento de la Ciberseguridad
- Política de Gobierno Electrónico del Ecuador

Estos instrumentos habilitan expresamente el intercambio interinstitucional de información y establecen los deberes de las entidades públicas de no requerir al ciudadano documentos que el Estado ya posee.
