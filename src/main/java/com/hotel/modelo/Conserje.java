package com.hotel.modelo;

import com.hotel.modelo.enums.TipoUsuario;
import com.hotel.modelo.enums.Turno;

/**
 * Representa la entidad de un Conserje (Trabajador del hotel).
 */
public final class Conserje extends Usuario{

    /** Salario mensual del conserje */
    private double salario;

    /** Turno en el que trabaja (Maniana, tarde, noche) */
    private Turno turno;

    /**
     * Constructor predeterminado.
     */
    public Conserje (){
        super(TipoUsuario.CONSERJE);
    }

    /**
     * Constructor completo para reinstanciar un Conserje desde la base de datos (con ID).
     *
     * @param usuarioId       Identificador único del usuario (PK en BD).
     * @param nombre          Nombre del pasajero.
     * @param apellido        Apellido del pasajero.
     * @param email           Correo electrónico.
     * @param dni             Documento Nacional de Identidad.
     * @param telefono        Teléfono de contacto.
     * @param nombreUsuario   Nombre de usuario.
     * @param contrasenia     Contraseña de acceso.
     * @param salario         Salario mensual.
     * @param turno           Turno de trabajo.
     */
    public Conserje (long usuarioId, String nombre, String apellido,
                     String email, String dni, String telefono,
                     String nombreUsuario, String contrasenia, double salario,
                     Turno turno){
        super(usuarioId, nombre, apellido, email, dni, telefono, nombreUsuario, contrasenia, TipoUsuario.CONSERJE);
        this.turno = turno;
        this.salario = salario;
    }

    /**
     * Constructor para registrar un nuevo Conserje antes de guardarlo en la BD (sin ID).
     *
     * @param nombre          Nombre del pasajero.
     * @param apellido        Apellido del pasajero.
     * @param email           Correo electrónico.
     * @param dni             Documento Nacional de Identidad.
     * @param telefono        Teléfono de contacto.
     * @param nombreUsuario   Nombre de usuario.
     * @param contrasenia     Contraseña de acceso.
     * @param salario         Salario mensual.
     * @param turno           Turno de trabajo.
     */
    public Conserje (String nombre, String apellido,
                     String email, String dni, String telefono,
                     String nombreUsuario, String contrasenia, double salario,
                     Turno turno){
        super(nombre, apellido, email, dni, telefono, nombreUsuario, contrasenia, TipoUsuario.CONSERJE);
        this.turno = turno;
        this.salario = salario;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }
}
