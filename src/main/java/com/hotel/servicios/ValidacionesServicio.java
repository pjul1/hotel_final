package com.hotel.servicios;

import com.hotel.excepciones.HotelException;

import java.sql.SQLException;

/**
 * Manejo de errores que comparten todos los servicios.
 * Es de uso interno del paquete 'servicios' (no es pública).
 */
final class ValidacionesServicio {

    /** Código de error de MySQL cuando se repite un valor UNIQUE (DNI, email, número de habitación). */
    private static final int ERROR_DUPLICADO = 1062;

    /** Clase de utilidades: no se instancia. */
    private ValidacionesServicio() {
    }

    /**
     * Convierte un SQLException en una HotelException con un mensaje claro para mostrar en pantalla.
     * Se conserva el error original como causa para poder depurarlo.
     *
     * @param accion Lo que se estaba haciendo (ej: "registrar la habitación").
     * @param e      Error original de la base de datos.
     */
    static HotelException errorDeBase(String accion, SQLException e) {
        String mensaje = "Error en la base de datos al " + accion + ".";

        if (e.getErrorCode() == ERROR_DUPLICADO && e.getMessage() != null) {
            // El mensaje de MySQL termina en "for key 'Tabla.columna'": se mira solo esa parte
            String detalle = e.getMessage().toLowerCase();
            int indice = detalle.lastIndexOf("for key");
            String clave = indice >= 0 ? detalle.substring(indice) : detalle;

            if (clave.contains("dni")) {
                mensaje = "Ya existe un usuario registrado con ese DNI.";
            } else if (clave.contains("email")) {
                mensaje = "Ya existe un usuario registrado con ese email.";
            } else if (clave.contains("numerohabitacion")) {
                mensaje = "Ya existe una habitación con ese número.";
            } else {
                mensaje = "Ya existe un registro con esos datos.";
            }
        }

        HotelException excepcion = new HotelException(mensaje);
        excepcion.initCause(e);
        return excepcion;
    }
}
