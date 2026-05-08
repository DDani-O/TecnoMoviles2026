# GUÍA DE REFACTORIZACIÓN Y MODULARIZACIÓN - FINTRACK MOBILE

Este documento sirve como estándar de oro para la refactorización de pantallas y componentes dentro del proyecto Fintrack. El objetivo es mantener una arquitectura sólida (MVVM + Clean Architecture) y una estética visual coherente (Pastel Moderno).

## 🚀 Objetivo Principal
Antes de crear nuevos componentes o lógica:
1.  **Analiza** la estructura actual del proyecto.
2.  **Verifica** si ya existen componentes reutilizables.
3.  **Reutiliza** módulos existentes siempre que tenga sentido.
4.  **Evita** duplicar código.
5.  **Mantén** consistencia visual y arquitectónica.

---

## 🛠 Prioridades

### 1. Reutilización Extrema
Antes de crear cualquier elemento visual (Cards, Inputs, Botones, etc.), verifica su existencia en:
*   `ui/components/`
*   `shared/` o `common/`
*   Screens similares.

**Regla de Oro:** Si un componente se usará en más de una pantalla, debe residir en `ui/components/` y no en carpetas locales de una funcionalidad. **Parametriza** en lugar de duplicar.

### 2. Arquitectura (MVVM + Clean)
*   **UI Declarativa:** El estado fluye hacia abajo, los eventos hacia arriba (State Hoisting).
*   **StateFlow:** Uso obligatorio de `collectAsStateWithLifecycle()`.
*   **Separación:** 
    *   0 lógica de negocio en Composables.
    *   0 acceso a datos en la capa de UI.
    *   Lógica pesada fuera de las recomposiciones.

### 3. Buenas Prácticas de Compose
*   Uso correcto de `remember`, `rememberSaveable` y `derivedStateOf`.
*   Uso de `keys` estables en `LazyColumn` y `LazyRow`.
*   Composables pequeños, especializados y con nombres descriptivos.
*   **Comentarios en español** explicando la función de cada componente.

### 4. Diseño Visual (Fintrack Pastel Style)
*   **Colores:** Uso exclusivo de `FintrackTheme.colors`. **PROHIBIDO** hardcodear colores o usar la clase `Color` directamente para elementos de marca.
*   **Formas:** Mantener consistencia con `RoundedCornerShape` (generalmente 12.dp a 24.dp).
*   **Material3:** Seguir los lineamientos de M3 adaptados a nuestra paleta.
*   **Coherencia:** El Login, Registro y Welcome deben sentirse parte del mismo universo que la HomeScreen.

### 5. Animaciones y Transiciones
*   Solo si aportan valor (ej. cambios de estado, feedback de carga).
*   Deben sentirse fluidas y profesionales, evitando "glitches" o saltos bruscos.

### 6. Organización de Archivos
Si una pantalla crece demasiado, se debe fragmentar en:
*   `components/[feature]/` (si son exclusivos).
*   Archivos de secciones o cards independientes.
*   Mantener el archivo de la Screen principal como un orquestador limpio.

---

## 📝 Check-list antes de entregar código
- [ ] ¿He revisado si el botón/input ya existía?
- [ ] ¿He usado `FintrackTheme.colors` en lugar de colores fijos?
- [ ] ¿He añadido comentarios en español?
- [ ] ¿El ViewModel maneja la lógica y la UI solo reacciona?
- [ ] ¿Es responsive y maneja estados de carga/error?

---
*Genera siempre código limpio, modular y listo para producción.*
