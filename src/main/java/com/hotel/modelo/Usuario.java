package com.hotel.modelo;

import com.hotel.modelo.enums.TipoUsuario;

/**
 * Representa la entidad base de un usuario en el sistema del hotel.
 * Esta clase sirve como superclase abstracta para Administrador, Conserje y Pasajero.
 */
public abstract class Usuario {

    /** Identificador único autogenerado en la base de datos (BIGINT). */
    private long usuarioId;

    /** Nombre del usuario. */
    private String nombre;

    /** Apellido del usuario. */
    private String apellido;

    /** Correo electrónico único del usuario. */
    private String email;

    /** Documento Nacional de Identidad (DNI) único del usuario. */
    private String dni;

    /** Número de teléfono de contacto del usuario. */
    private String telefono;

    /** Nombre de usuario para el inicio de sesión. */
    private String nombreUsuario;

    /** Contraseña para la autenticación en el sistema. */
    private String contrasenia;

    /** Estado del usuario en el sistema para la baja lógica. */
    private boolean activo;

    /** Tipo de usuario (Administrador, Conserje, Pasajero). */
    private TipoUsuario tipoUsuario;

    /**
     * Constructor predeterminado.
     * Inicializa el estado del usuario como activo por defecto.
     */
    public Usuario() {
        this.activo = true;
    }

    /**
     * Constructor completo para reinstanciar usuarios existentes desde la base de datos.
     *
     * @param usuarioId     Identificador único del usuario (PK en BD).
     * @param nombre        Nombre del usuario.
     * @param apellido      Apellido del usuario.
     * @param email         Correo electrónico del usuario.
     * @param dni           Documento Nacional de Identidad.
     * @param telefono      Teléfono de contacto.
     * @param nombreUsuario Credencial de usuario para login.
     * @param contrasenia   Contraseña de acceso.
     */
    public Usuario( long usuarioId, String nombre, String apellido,
                    String email, String dni, String telefono,
                    String nombreUsuario, String contrasenia,
                    TipoUsuario tipoUsuario){

        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.dni = dni;
        this.telefono = telefono;
        this.nombreUsuario = nombreUsuario;
        this.contrasenia = contrasenia;
        this.activo = true; // 'activo' Está activo de forma predeterminada.
        this.tipoUsuario = tipoUsuario;
    }

    /**
     * Constructor utilizado para la creación de un nuevo usuario en la aplicación
     * previo a ser insertado en la base de datos (el 'id' se asignará por Auto-Increment).
     *
     * @param nombre        Nombre del usuario.
     * @param apellido      Apellido del usuario.
     * @param email         Correo electrónico del usuario.
     * @param dni           Documento Nacional de Identidad.
     * @param telefono      Teléfono de contacto.
     * @param nombreUsuario Credencial de usuario para login.
     * @param contrasenia   Contraseña de acceso.
     */
     public Usuario( String nombre, String apellido,
                    String email, String dni, String telefono,
                    String nombreUsuario, String contrasenia, TipoUsuario tipoUsuario){

         this.nombre = nombre;
         this.apellido = apellido;
         this.email = email;
         this.dni = dni;
         this.telefono = telefono;
         this.nombreUsuario = nombreUsuario;
         this.contrasenia = contrasenia;
         this.activo = true; // 'activo' Está activo de forma predeterminada.
         this.tipoUsuario = tipoUsuario;
     }

     @Override
     public String toString(){
         return "ID="+usuarioId+", nombre=" +nombre+ ", apellido="+apellido+", email=" +email+
                 ", dni=" +dni+", telefono=" +telefono+ ", nombreUsuario=" +nombreUsuario+
                 ", contrasenia="+contrasenia+ "; está activo=" +activo+
                 ", tipo de usuario="+tipoUsuario+"]";
     }
     @Override
     public int hashCode(){
        return Long.hashCode(usuarioId);
     }

    /**
     * Método equals usa el ID para evaluar igualdad entre entidades.
     */
     @Override
     public boolean equals(Object o){
        if(this == o) { return true; }
        if(o == null || this.getClass() != o.getClass()) {return false;}
        Usuario usuario = (Usuario)o;
         return this.usuarioId == usuario.usuarioId;
     }

     /** GETTERS Y SETTERS */

     public long getUsuarioId() {
            return usuarioId;
     }

     public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
     }

     public String getNombre() {
        return nombre;
     }

     public void setNombre(String nombre) {
        this.nombre = nombre;
     }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }
}
