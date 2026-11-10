package com.hotel.modelo;

import com.hotel.modelo.enums.TipoUsuario;

/**
 * Representa la entidad de un Administrador dentro del sistema del hotel.
 */
public class Administrador extends Usuario {

    /**
     * Constructor completo para reinstanciar un Administrador desde la base de datos (con ID).
     *
     * @param usuarioId     Identificador único del usuario (PK en BD).
     * @param nombre        Nombre del administrador.
     * @param apellido      Apellido del administrador.
     * @param email         Correo electrónico.
     * @param dni           Documento Nacional de Identidad.
     * @param telefono      Teléfono de contacto.
     * @param nombreUsuario Credencial de usuario para el login.
     * @param contrasenia   Contraseña de acceso.
     */
    public Administrador(long usuarioId, String nombre, String apellido, String email,
                         String dni, String telefono, String nombreUsuario, String contrasenia,
                         TipoUsuario tipoUsuario) {
        super(usuarioId, nombre, apellido, email, dni, telefono, nombreUsuario, contrasenia, tipoUsuario);
    }

    /**
     * Constructor utilizado para registrar un nuevo Administrador previo a guardarlo en la BD.
     *
     * @param nombre        Nombre del administrador.
     * @param apellido      Apellido del administrador.
     * @param email         Correo electrónico.
     * @param dni           Documento Nacional de Identidad.
     * @param telefono      Teléfono de contacto.
     * @param nombreUsuario Credencial de usuario para el login.
     * @param contrasenia   Contraseña de acceso.
     */
    public Administrador(String nombre, String apellido, String email, String dni,
                         String telefono, String nombreUsuario, String contrasenia,
                         TipoUsuario tipoUsuario) {
        super(nombre, apellido, email, dni, telefono, nombreUsuario, contrasenia, tipoUsuario);
    }

    /**
     * Constructor predeterminado.
     */
    public Administrador() {
        super();
    }

}