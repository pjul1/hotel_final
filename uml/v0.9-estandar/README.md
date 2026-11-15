# UML V0.9 estándar (propuesta para revisar)

Diagrama de clases actualizado según el código de `main` (commit `ab3c775`).
Se sube en una carpeta aparte para que el equipo lo revise. **No reemplaza** a `uml/UML-hotel.html`: si lo aprobamos, se puede reemplazar después.

## Archivos

| Archivo | Para qué sirve |
|---|---|
| `UML_Hotel_V0.9_estandar.drawio` | El diagrama editable. Se abre en <https://app.diagrams.net> (Archivo → Abrir desde → Dispositivo). |
| `UML-hotel_V0.9_estandar.html` | El mismo diagrama para ver en el navegador. Tiene el mismo formato que `uml/UML-hotel.html`. |
| `UML_Hotel_V0.9_paquetes_resumen.pdf` | Resumen corto (3 páginas): lo más importante y las conexiones entre paquetes. |
| `UML_Hotel_V0.9_relaciones_y_conexiones.pdf` | Documento completo (16 páginas): relaciones de cada clase, paquetes y capas, y todos los tipos de conexión UML. |

## Qué cambia respecto del UML actual (V0.8a)

- Refleja el código real: vistas JavaFX en lugar de menús de consola, un servicio por entidad en lugar de `Hotel` y los gestores, los 5 DAO, las 4 interfaces y `ConexionDB` en `config`.
- Muestra todos los atributos y métodos de los servicios, DAO y vistas, incluidos los privados.
- Marca las clases `final` («final») y los atributos `final` ({readOnly}).
- Cada enum es una caja «enumeration» dentro del subpaquete `com.hotel.modelo.enums`, con una asociación desde la clase que lo usa.
- No dibuja `RuntimeException` (clase de Java).
- 37 elementos, 9 paquetes y 67 conexiones, sin cruces entre líneas.

## Para revisar

- ¿Falta o sobra alguna clase, atributo o método?
- ¿Están bien las relaciones entre clases y paquetes?
- ¿Lo usamos como UML oficial y reemplazamos `uml/UML-hotel.html`?
