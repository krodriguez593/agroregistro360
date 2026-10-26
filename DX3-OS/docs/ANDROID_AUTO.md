# Android Auto en el Soueast DX3 — qué es viable

## Tres cosas distintas

| | Qué es | Dónde corre | ¿Lo puede hacer DX3 OS? |
|---|---|---|---|
| **Android Auto (proyectado)** | El teléfono genera la interfaz y la envía a la pantalla del coche | Teléfono + **receptor certificado** en la radio | **No.** Una app de terceros no puede convertirse en receptor. Requiere una radio que lo traiga de fábrica, con licencia de Google |
| **Android Automotive OS (AAOS)** | Sistema operativo completo del vehículo, integrado por el fabricante (con VHAL, servicios del coche y, opcionalmente, servicios de Google con licencia) | La propia radio | **No.** Solo lo integran fabricantes de vehículos/proveedores Tier-1 con el hardware y los acuerdos correspondientes |
| **Interfaz personalizada** (DX3 OS) | Un launcher con aspecto moderno | La radio Android existente | Sí — y **no se presenta como Android Auto** |

## Política del proyecto

- DX3 OS **no implementa, no imita y no intenta eludir** el protocolo, la autenticación ni la
  certificación de Android Auto.
- La tarjeta "Android Auto" de Inicio muestra el estado real: **"sin verificar"** hasta que el
  informe de hardware confirme que la radio es un receptor certificado.
- Si se confirma un receptor del fabricante, DX3 OS solo ofrecerá un acceso directo para abrirlo.

## Cómo verificar si la radio actual lo soporta

1. Revisar el manual del vehículo y el menú de la radio (buscar "Android Auto", "Phone link").
2. Conectar un teléfono Android con cable USB de datos y con la app/función Android Auto activa.
3. Revisar paquetes instalados con el diagnóstico de solo lectura (`50-packages-relevant.txt`).
4. Ojo: las apps de "espejo" o "enlace" incluidas en muchas radios genéricas no siempre son
   receptores certificados; su estado de licencia debe comprobarse con su fabricante.

## Alternativas compatibles si no está soportado

1. **Radio de reemplazo certificada** para Android Auto (marcas de audio reconocidas publican la
   lista de compatibilidad). Debe ser compatible con el marco/arnés del DX3, con la cámara de
   reversa y, si se desea, con un adaptador de mandos del volante. Instalación profesional.
2. **Adaptadores inalámbricos**: solo funcionan si la radio **ya** soporta Android Auto con cable.
   No añaden compatibilidad a una radio que no la tiene.
3. **Teléfono en soporte** con Google Maps en modo conducción: alternativa legal y sin hardware.
4. Seguir usando DX3 OS como launcher independiente: navegación, música y teléfono con las apps
   instaladas en la radio.
