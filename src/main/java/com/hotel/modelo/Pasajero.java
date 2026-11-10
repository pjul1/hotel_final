package com.hotel.modelo;


import com.hotel.modelo.enums.TipoUsuario;

/**
 * Representa la entidad de un Pasajero (cliente del hotel).
 */
public class Pasajero extends Usuario {

    /** Ciudad o provincia de origen del pasajero. */
    private String origen;

    /** Domicilio de origen del pasajero. */
    private String domicilioOrigen;

    /**
     * Constructor predeterminado.
     */
    public Pasajero(String origen, String domicilioOrigen) {
        super();
    }

    /**
     * Constructor completo para reinstanciar un Pasajero desde la base de datos (con ID).
     *
     * @param usuarioId       Identificador único del usuario (PK en BD).
     * @param nombre          Nombre del pasajero.
     * @param apellido        Apellido del pasajero.
     * @param email           Correo electrónico.
     * @param dni             Documento Nacional de Identidad.
     * @param telefono        Teléfono de contacto.
     * @param nombreUsuario   Nombre de usuario.
     * @param contrasenia     Contraseña de acceso.
     * @param origen          Lugar de origen.
     * @param domicilioOrigen Domicilio de origen.
     */
    public Pasajero(
            long usuarioId, String nombre, String apellido,
            String email, String dni, String telefono,
            String nombreUsuario, String contrasenia, String origen, String domicilioOrigen,
            TipoUsuario tipoUsuario) {
        super(usuarioId, nombre, apellido, email, dni, telefono, nombreUsuario, contrasenia, tipoUsuario);
        this.origen = origen;
        this.domicilioOrigen = domicilioOrigen;
    }

    /**
     * Constructor para registrar un nuevo Pasajero antes de guardarlo en la BD (sin ID).
     *
     * @param nombre          Nombre del pasajero.
     * @param apellido        Apellido del pasajero.
     * @param email           Correo electrónico.
     * @param dni             Documento Nacional de Identidad.
     * @param telefono        Teléfono de contacto.
     * @param nombreUsuario   Nombre de usuario.
     * @param contrasenia     Contraseña de acceso.
     * @param origen          Lugar de origen.
     * @param domicilioOrigen Domicilio de origen.
     */
    public Pasajero(
                    String nombre, String apellido,
                    String email, String dni, String telefono,
                    String nombreUsuario, String contrasenia, String origen, String domicilioOrigen,
                    TipoUsuario tipoUsuario) {
        super(nombre, apellido, email, dni, telefono, nombreUsuario, contrasenia, tipoUsuario);
        this.origen = origen;
        this.domicilioOrigen = domicilioOrigen;
    }

    @Override
    public String toString() {
       return super.toString() + ", [origen=" + origen + ", domicilioOrigen=" + domicilioOrigen + "]" ;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDomicilioOrigen() {
        return domicilioOrigen;
    }

    public void setDomicilioOrigen(String domicilioOrigen) {
        this.domicilioOrigen = domicilioOrigen;
    }
}
