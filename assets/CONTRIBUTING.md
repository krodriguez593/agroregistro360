# Guía de contribución

## Cómo aportar al proyecto

Este es un proyecto académico del Diplomado en Innovación Pública 2026. Las contribuciones internas del equipo siguen este flujo:

### Flujo de trabajo

1. **Crear una rama** desde `main` con nombre descriptivo:
   ```bash
   git checkout -b feat/nombre-de-la-funcionalidad
   git checkout -b fix/descripcion-del-fix
   git checkout -b docs/actualizacion-de-doc
   ```

2. **Hacer cambios pequeños** y comitear con mensaje claro:
   ```bash
   git commit -m "feat: agregar validación de cédula"
   git commit -m "fix: corregir cálculo de indicador EF-01"
   git commit -m "docs: actualizar sección de interoperabilidad"
   ```

3. **Abrir un Pull Request** describiendo:
   - Qué se cambió y por qué
   - Cómo probarlo
   - Screenshots si aplica

4. **Revisión**: al menos un miembro del equipo revisa antes de merge.

### Convención de commits

Seguimos [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` nueva funcionalidad
- `fix:` corrección de bug
- `docs:` cambios en documentación
- `style:` formato, sin cambios de código
- `refactor:` reestructuración sin cambio funcional
- `test:` agregar o corregir pruebas
- `chore:` tareas de mantenimiento

### Estructura del prototipo

El prototipo está en un solo archivo `index.html` por decisión de diseño (portabilidad y simplicidad académica). Al modificarlo:

- Mantener las tres capas separadas: `<style>`, `<body>`, `<script>`.
- Cada vista tiene su propia función `viewX()`.
- Los datos ficticios están en `src/data/mock-db.json` (referencia); en el prototipo actual están embebidos en el script como constante `DB`.
- Los estilos usan variables CSS (`--purple`, etc.) — no hardcodear colores.

### Antes de hacer merge

- [ ] El prototipo abre correctamente en Chrome, Firefox y Safari.
- [ ] Modo claro y modo oscuro se ven bien.
- [ ] En móvil no hay scroll horizontal.
- [ ] Las tres cédulas de prueba funcionan.
- [ ] El flujo completo de productor termina con éxito.
- [ ] La vista de funcionario permite aprobar/observar.
- [ ] El tablero de indicadores muestra los gráficos.

### Datos ficticios

**Nunca** subir datos reales de productores, cédulas o RUCs al repositorio. Todos los datos son ficticios y así deben permanecer.

Si necesitás agregar un nuevo perfil de prueba, editá `src/data/mock-db.json` con un patrón coherente (todas las cinco entidades deben tener registro para esa cédula, o dejar `null` explícito).

### Contacto

Coordinar cambios grandes con Kevin Rodríguez (Planificación y monitoreo) antes de empezar, para no duplicar trabajo.
