# FintrackMobile - Documentación Técnica del Proyecto

Bienvenido a la documentación técnica de **FintrackMobile**, una aplicación Android moderna diseñada para el seguimiento de gastos, análisis de ofertas y gestión de tickets de supermercado. Este documento detalla la arquitectura, el flujo de datos y las mejores prácticas aplicadas en el desarrollo.

---

## 1. Arquitectura General

La aplicación sigue los principios de **Clean Architecture** y el patrón de diseño **MVVM (Model-View-ViewModel)**, asegurando una separación clara de responsabilidades y facilitando la escalabilidad y el mantenimiento.

### Capas de la Aplicación

| Capa | Responsabilidad | Componentes Clave |
| :--- | :--- | :--- |
| **UI (Vista)** | Representación visual y captura de eventos del usuario. | Jetpack Compose, Screens, Components. |
| **ViewModel** | Manejo del estado de la UI y lógica de presentación. | `StateFlow`, `viewModelScope`, `UiState`. |
| **Repository (Data)** | Fuente de verdad de los datos. Abstrae el origen (Local/Remoto). | `PurchaseRepository`, `ExploreRepository`. |
| **Data Source** | Acceso a la base de datos local o APIs externas. | Room DAO, SharedPreferences/DataStore. |

### Diagrama de Flujo de Datos
```text
[ Usuario ] <---> [ UI (Compose) ] <--- (Observa) --- [ StateFlow ]
                                                          ^
                                                          | (Actualiza)
                                                   [ ViewModel ]
                                                          |
                                                          v
                                                   [ Repository ]
                                                          |
                                         ----------------------------------
                                         |                                |
                                 [ Local DB (Room) ]              [ Remote API / Mock ]
```

---

## 2. Estructura del Proyecto

La estructura de carpetas está organizada por capas y funcionalidades:

- `data/`: Contiene la lógica de acceso a datos.
    - `local/`: Entidades de Room y DAOs.
    - `repository/`: Implementaciones de los repositorios que coordinan los datos.
- `ui/`: Todo lo relacionado con la interfaz de usuario.
    - `screens/`: Composables de alto nivel que representan una pantalla completa.
    - `components/`: Componentes atómicos y modulares organizados por feature (home, explore, records, etc.).
    - `theme/`: Definición del sistema de diseño (Colores pastel, Tipografía, Shapes).
    - `util/`: Funciones de utilidad para formateo (moneda, fechas) y diálogos.
- `viewmodel/`: Lógica de negocio de la UI y estados (Sealed Classes).

---

## 3. Funcionamiento de Jetpack Compose

La aplicación utiliza una UI **totalmente declarativa**. En lugar de manipular vistas manualmente, describimos cómo debe verse la UI para un estado dado.

### Conceptos Clave Aplicados:
- **Recomposición**: Compose actualiza automáticamente solo las partes de la UI que dependen de un estado modificado.
- **State Hoisting**: El estado se "eleva" al componente de nivel superior (usualmente el ViewModel o la Screen) para que los componentes hijos sean puros y fáciles de testear.
- **Stable Keys**: En listas (`LazyColumn`/`LazyRow`), se utilizan `key = { it.id }` para optimizar el rendimiento y evitar recomposiciones innecesarias de elementos que no han cambiado.

```kotlin
// Ejemplo de State Hoisting
@Composable
fun LabeledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    // ...
) {
    OutlinedTextField(value = value, onValueChange = onValueChange, ...)
}
```

---

## 4. Manejo de Estado (UiState)

El estado de la interfaz se maneja mediante clases selladas (**Sealed Classes**) y `StateFlow`, lo que garantiza que la UI siempre tenga un estado definido (Cargando, Éxito o Error).

### Ejemplo de Estructura de Estado:
```kotlin
sealed class ExploreUiState {
    object Cargando : ExploreUiState()
    data class Exito(val datos: DatosExplore) : ExploreUiState()
    data class Error(val mensaje: String) : ExploreUiState()
}
```

La UI reacciona mediante `collectAsStateWithLifecycle()`, que es consciente del ciclo de vida de Android, evitando fugas de memoria y consumo innecesario de recursos.

---

## 5. ViewModels y Corrutinas

El `ViewModel` actúa como el cerebro de la pantalla. Se comunica con los repositorios usando **Corrutinas** de Kotlin dentro del `viewModelScope`.

- **Responsabilidad**: Transformar datos crudos del repositorio en un estado que la UI pueda consumir fácilmente.
- **Asincronismo**: Las operaciones de base de datos o red se ejecutan en hilos secundarios (Dispatchers.IO) para no bloquear la interfaz.

---

## 6. Navegación

Se utiliza **Navigation Compose** para gestionar el flujo entre pantallas mediante un `NavHost`. Las rutas están definidas de forma declarativa, permitiendo una navegación fluida y segura.

```kotlin
NavHost(navController = navController, startDestination = "home") {
    composable("home") { HomeScreen(...) }
    composable("explore") { ExploreScreen(...) }
    // ...
}
```

---

## 7. Componentes Reutilizables y Modularidad

Para mantener el código limpio y evitar archivos gigantes, se han extraído componentes específicos a carpetas dedicadas:

- **Modularidad**: Componentes como `SupermercadoCard` o `HistoryRecordCard` viven en archivos separados.
- **Common Components**: Elementos que se usan en más de una pantalla (ej. `FintrackSectionHeader`) se encuentran en la raíz de `components/`.

---

## 8. Diseño Visual (FintrackTheme)

La app implementa un sistema de diseño basado en **Material3** con una personalización profunda:

- **Colores Pastel**: Definidos en `FintrackBrandColors`, proporcionan una estética moderna y amigable.
- **Consistencia**: El uso de `FintrackTheme.colors` asegura que el cambio de un color se refleje en toda la aplicación instantáneamente.
- **Shapes**: Se priorizan los `RoundedCornerShape(24.dp)` para una sensación suave y profesional.

---

## 9. Flujo Completo de un Dato (Ejemplo)

1. **Usuario**: Toca el botón "Guardar Compra".
2. **UI**: Llama a `viewModel.savePurchase(data)`.
3. **ViewModel**: Valida los datos y lanza una corrutina. Llama a `repository.insertPurchase(entity)`.
4. **Repository**: Inserta el dato en la base de datos de Room.
5. **Base de Datos**: Emite un nuevo flujo de datos (Flow).
6. **ViewModel**: Recibe el Flow, actualiza el `MutableStateFlow` de la UI.
7. **UI**: Se entera del cambio y se "redibuja" (Recomposición) para mostrar la nueva compra.

---

## 10. Buenas Prácticas Aplicadas

- **DRY (Don't Repeat Yourself)**: Uso intensivo de componentes compartidos.
- **KISS (Keep It Simple, Stupid)**: Lógica de UI simple y declarativa.
- **Optimización**: Uso de `derivedStateOf` para cálculos complejos y `remember` para persistir estados locales.
- **Documentación**: Código comentado en español para facilitar la colaboración.

---
Este documento sirve como base para cualquier desarrollador que desee contribuir al proyecto **FintrackMobile**.
