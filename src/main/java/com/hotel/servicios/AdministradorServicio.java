package com.hotel.servicios;

import com.hotel.dao.AdministradorDao;
import com.hotel.excepciones.HotelException;
import com.hotel.modelo.Administrador;

import java.sql.SQLException;

/**
 * Lógica de negocio del administrador único del sistema.
 * Mantiene en memoria la instancia cargada desde la base de datos para evitar consultas repetidas
 * y valida sus datos antes de actualizarla.
 */
public class AdministradorServicio {

    private final AdministradorDao administradorDao = new AdministradorDao();
    private Administrador administradorEnMemoria;

    /** Carga el administrador desde la base de datos al iniciar el servicio. */
    public AdministradorServicio() {
        cargarDesdeBase();
    }

    /** Devuelve el administrador único del sistema (lo trae de memoria si ya está cargado). */
    public Administrador obtenerAdministrador() {
        if (administradorEnMemoria == null) {
            cargarDesdeBase();
        }
        return administradorEnMemoria;
    }

    /**
     * Valida y actualiza los datos personales y credenciales del administrador en la base de datos
     * y sincroniza la instancia en memoria.
     */
    public void modificar(Administrador administrador) {
        validarAdministrador(administrador);

        try {
            Administrador actual = obtenerAdministrador();
            if (actual == null) {
                throw new HotelException("No se encontró el administrador configurado en el sistema.");
            }

            // Se conserva el ID y el estado activo real de la base
            administrador.setUsuarioId(actual.getUsuarioId());
            administrador.setActivo(actual.isActivo());

            administradorDao.modificar(administrador);
            administradorEnMemoria = administrador;
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("modificar los datos del administrador", e);
        }
    }

    /** Verifica que los campos obligatorios del administrador no estén vacíos. */
    private void validarAdministrador(Administrador admin) {
        if (admin == null) {
            throw new HotelException("Los datos del administrador son obligatorios.");
        }
        if (esVacio(admin.getNombre()) || esVacio(admin.getApellido())) {
            throw new HotelException("El nombre y el apellido del administrador son obligatorios.");
        }
        if (esVacio(admin.getDni())) {
            throw new HotelException("El DNI del administrador es obligatorio.");
        }
        if (esVacio(admin.getEmail()) || !admin.getEmail().contains("@")) {
            throw new HotelException("Debe ingresar un email válido.");
        }
        if (esVacio(admin.getTelefono())) {
            throw new HotelException("El teléfono del administrador es obligatorio.");
        }
        if (esVacio(admin.getNombreUsuario())) {
            throw new HotelException("El nombre de usuario es obligatorio.");
        }
        if (esVacio(admin.getContrasenia()) || admin.getContrasenia().length() < 4) {
            throw new HotelException("La contraseña debe tener al menos 4 caracteres.");
        }
    }

    /** Carga la cuenta única del administrador desde MySQL. */
    private void cargarDesdeBase() {
        try {
            administradorEnMemoria = administradorDao.obtenerAdministrador();
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("cargar el administrador", e);
        }
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}