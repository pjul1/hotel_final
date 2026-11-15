package com.hotel.modelo;

import com.hotel.modelo.enums.EstadoHabitacion;
import com.hotel.modelo.enums.EstadoReserva;
import com.hotel.modelo.enums.MotivoBloqueo;
import com.hotel.modelo.enums.TipoHabitacion;
import com.hotel.servicios.ReservaServicio;

/** Representa cada habitación dentro del hotel */
public final class Habitacion {

    /** Identificador único autogenerado en la base de datos (BIGINT). */
    private long habitacionId;

    /** Identificador único de cada habitación individual. */
    private int numeroHabitacion;

    /** Representa el tipo de una habitación (Simple, Doble, Suite). */
    private TipoHabitacion tipoHabitacion;

    /** Representa la disponibilidad actual de la habitación (Disponible, No Disponible, Reservada, Ocupada). */
    private EstadoHabitacion estadoHabitacion;

    /** Representa el motivo de bloqueo actual de la habitación (Limpieza, Desinfección, etc.). */
    private MotivoBloqueo motivoBloqueo;

    /** Estado de la habitación en el sistema para la baja lógica. */
    private boolean activo;

    /**
     * Constructor predeterminado sin argumentos.
     */
    public Habitacion(){
        this.estadoHabitacion = EstadoHabitacion.DISPONIBLE; // Estado predeterminado.
        this.motivoBloqueo = null; // No hay motivo de bloqueo de forma predeterminada.
        this.activo = true; // Está activo de forma predeterminada.
    }

    /**
     * Constructor completo para reinstanciar una habitación existente desde la base de datos.
     *
     * @param habitacionId     Identificador único en la BD.
     * @param numeroHabitacion Número físico de habitación.
     * @param tipoHabitacion   Tipo de habitación.
     * @param estadoHabitacion Estado actual de disponibilidad.
     * @param motivoBloqueo    Motivo de bloqueo si aplica (o null).
     * @param activo           Estado de baja lógica.
     */
    public Habitacion (long habitacionId, int numeroHabitacion, TipoHabitacion tipoHabitacion,
                       EstadoHabitacion estadoHabitacion, MotivoBloqueo motivoBloqueo, boolean activo){
        this.habitacionId = habitacionId;
        this.numeroHabitacion = numeroHabitacion;
        this.tipoHabitacion = tipoHabitacion;
        this.estadoHabitacion = estadoHabitacion;
        this.motivoBloqueo = motivoBloqueo;
        this.activo = activo;

    }


    public Habitacion (int numeroHabitacion, TipoHabitacion tipoHabitacion){
        this.numeroHabitacion = numeroHabitacion;
        this.tipoHabitacion = tipoHabitacion;
        this.estadoHabitacion = EstadoHabitacion.DISPONIBLE; // Estado predeterminado.
        this.motivoBloqueo = null; // No hay motivo de bloqueo de forma predeterminada.
        this.activo = true; // Está activo de forma predeterminada.

    }

    @Override
    public String toString(){
        return "[Id="+habitacionId+ ", numero de habitacion="+numeroHabitacion+
                ", tipo de habitacion=" +tipoHabitacion+
                "; estado de la habitacion="+estadoHabitacion+
                ", motivo de bloqueo=" +motivoBloqueo+ ", esta activo="+activo+ "]";
    }
    @Override
    public int hashCode(){
        return Long.hashCode(habitacionId);

    }
    @Override
    public boolean equals(Object o){
        if(this == o) { return true; }
        if(o == null || o.getClass() != this.getClass()) { return false; }
        Habitacion habitacion = (Habitacion) o;
        return habitacion.getHabitacionId() == this.habitacionId;
    }

    public long getHabitacionId() {
        return habitacionId;
    }

    public void setHabitacionId(long habitacionId) {
        this.habitacionId = habitacionId;
    }

    public int getNumeroHabitacion() {
        return numeroHabitacion;
    }

    public void setNumeroHabitacion(int numeroHabitacion) {
        this.numeroHabitacion = numeroHabitacion;
    }

    public TipoHabitacion getTipoHabitacion() {
        return tipoHabitacion;
    }

    public void setTipoHabitacion(TipoHabitacion tipoHabitacion) {
        this.tipoHabitacion = tipoHabitacion;
    }

    public EstadoHabitacion getEstadoHabitacion() {
        return estadoHabitacion;
    }

    public void setEstadoHabitacion(EstadoHabitacion estadoHabitacion) {
        this.estadoHabitacion = estadoHabitacion;
    }

    public MotivoBloqueo getMotivoBloqueo() {
        return motivoBloqueo;
    }

    public void setMotivoBloqueo(MotivoBloqueo motivoBloqueo) {
        this.motivoBloqueo = motivoBloqueo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getOcupanteActual() {
        if (this.estadoHabitacion == EstadoHabitacion.OCUPADA) {
            for (Reserva r : ReservaServicio.getInstancia().listar()) {
                if (r.getHabitacion() != null &&
                        r.getHabitacion().getHabitacionId() == this.habitacionId &&
                        r.getEstadoReserva() == EstadoReserva.ACTIVA &&
                        r.getCheckIn() != null) {

                    if (r.getPasajero() != null) {
                        return r.getPasajero().getNombre() + " " + r.getPasajero().getApellido();
                    }
                }
            }
        }
        return "-";
    }

    public String getDniOcupanteActual() {
        if (this.estadoHabitacion == EstadoHabitacion.OCUPADA) {
            for (Reserva r : ReservaServicio.getInstancia().listar()) {
                if (r.getHabitacion() != null &&
                        r.getHabitacion().getHabitacionId() == this.habitacionId &&
                        r.getEstadoReserva() == EstadoReserva.ACTIVA &&
                        r.getCheckIn() != null) {

                    if (r.getPasajero() != null) {
                        return r.getPasajero().getDni();
                    }
                }
            }
        }
        return "-";
    }
}
