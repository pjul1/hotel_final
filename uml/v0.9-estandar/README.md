# UML V0.9 estándar (versión final)

Diagrama de clases según el código de `main` (commit `9c01e66`, hasta el PR #26) más la corrección de los servicios (rama `fix/servicios-sincronizacion`).
La versión anterior del equipo (V0.8a) está en `uml/v0.8/UML-hotel.html`.

## Archivos

| Archivo | Para qué sirve |
|---|---|
| `UML_Hotel_V0.9_estandar.drawio` | El diagrama editable. Se abre en <https://app.diagrams.net> (Archivo → Abrir desde → Dispositivo). |
| `UML-hotel_V0.9_estandar.html` | El mismo diagrama para ver en el navegador. |
| `UML_Hotel_V0.9_paquetes_resumen.pdf` | Resumen corto (3 páginas): lo más importante y las conexiones entre paquetes. |
| `UML_Hotel_V0.9_relaciones_y_conexiones.pdf` | Documento completo (20 páginas): relaciones de cada clase, paquetes y capas, y todos los tipos de conexión UML. |

## Qué muestra

- Las 6 ventanas JavaFX, los 6 servicios (Singleton), los 5 DAO, `ConexionDB`, el modelo, las 4 interfaces y las 3 excepciones.
- Todos los atributos y métodos de servicios, DAO y vistas, incluidos los privados (getters, setters y campos `@FXML` agrupados).
- Clases `final` («final») y atributos `final` ({readOnly}).
- Cada enum es una caja «enumeration» dentro del subpaquete `com.hotel.modelo.enums`, con una asociación desde la clase que lo usa.
- No dibuja `RuntimeException` (clase de Java).
- 39 elementos, 9 paquetes y 71 conexiones, sin cruces entre líneas. Verificado contra el código compilado (`javap` y `jdeps`).

## Cambios respecto de la versión anterior del repositorio

- **Servicios corregidos:** se quitó `-liberarSiEstaReservada()` de `ReservaServicio`; se agregaron `-copiarDatos()` en `HabitacionServicio` y `-restaurarDesdeBase()` en `AdministradorServicio`, `ConserjeServicio` y `PasajeroServicio`.
- **Vistas (PR #26):** `ConserjeVentana` y `PasajeroVentana` ya no tienen `-habitacionDisponibleEnFechas()`: piden las habitaciones libres a `HabitacionServicio.listarDisponiblesEnPeriodo()`.
- **Hasta el PR #24:** `ConserjeVentana`, `PasajeroVentana` completa, login con los servicios, métodos nuevos de `Habitacion` y `Reserva`, y las conexiones `vistas → servicios`, `vistas → excepciones` y `modelo → servicios`.
- Los dos PDF están actualizados con todo lo anterior.

## Notas de diseño (se ven en el diagrama)

- `modelo → servicios` es una dependencia circular: `Habitacion.getOcupanteActual()` llama a `ReservaServicio.getInstancia()`.
- `vistas → dao` aparece solo por el atributo `pasajeroDao` de `CrearCuentaVentana`, que ya no se usa.
