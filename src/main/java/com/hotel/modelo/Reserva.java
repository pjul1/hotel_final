package com.hotel.modelo;

import com.hotel.modelo.enums.EstadoReserva;

import java.time.LocalDateTime;

/**  Representa la reserva de una habitación del hotel. */
public final class Reserva {

    /** Identificador único de la reserva. */
    private long reservaId;

    /** Precio de la reserva. */
    private double precio;

    /** Cantidad de pasajeros esperados para acomodarse. */
    private int cantidadPasajeros;

    /** El estado en el que la reserva se encuentra (Activa, Cancelada, Cumplida). */
    private EstadoReserva estadoReserva;

    /** Días que se esperan quedar los pasajeros. */
    private int duracionEstadia;

    /** Día reservado para una habitación. */
    private LocalDateTime diaReservado;

    /** Tiempo exacto en el que los pasajeros llegaron realmente. */
    private LocalDateTime checkIn;

    /** Tiempo exacto en el que los pasajeros se fueron realmente. */
    private LocalDateTime checkOut;

    /** Habitación a la que pertenece la reserva. */
    private Habitacion habitacion;

    /** Pasajero que compro la reserva. */
    private Pasajero pasajero;

    public Reserva(){
        this.estadoReserva = EstadoReserva.ACTIVA; // Valor predeterminado.
        this.checkIn = null; // No existe check in aun.
        this.checkOut = null; // No existe check out aun.
    }
    public Reserva(long reservaId, double precio, int cantidadPasajeros, EstadoReserva estadoReserva, int duracionEstadia,
                   LocalDateTime diaReservado, LocalDateTime checkIn, LocalDateTime checkOut,
                   Habitacion habitacion, Pasajero pasajero){
        this.estadoReserva = estadoReserva;
        this.diaReservado = diaReservado;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.reservaId = reservaId;
        this.precio = precio;
        this.cantidadPasajeros= cantidadPasajeros;
        this.duracionEstadia = duracionEstadia;
        this.habitacion = habitacion;
        this.pasajero = pasajero;
    }
    public Reserva(double precio, int cantidadPasajeros, int duracionEstadia, LocalDateTime diaReservado,
                   Habitacion habitacion, Pasajero pasajero){
        this.estadoReserva = EstadoReserva.ACTIVA; // Valor predeterminado.
        this.diaReservado = diaReservado;
        this.checkIn = null; // No existe check in aun.
        this.checkOut = null; // No existe check out aun.
        this.precio = precio;
        this.cantidadPasajeros= cantidadPasajeros;
        this.duracionEstadia = duracionEstadia;
        this.habitacion = habitacion;
        this.pasajero = pasajero;
    }

    @Override
    public String toString(){
        String auxHabitacion = "Sin Habitación";
        String auxPasajero = "Sin Pasajero";
        if(!(this.habitacion==null)){ auxHabitacion = String.valueOf(habitacion.getNumeroHabitacion()); }
        if(!(this.pasajero==null)){ auxPasajero = String.valueOf(pasajero.getDni()); }

        return  "[Id=" +reservaId+ ", precio=" +precio+ ", cantidad de pasajeros=" +cantidadPasajeros+
                ", estado de la reserva=" +estadoReserva+
                ", duración de la estadía=" +duracionEstadia+
                ", día reservado=" +diaReservado+ ", check-in=" +checkIn+ ", check-out=" +checkOut+
                ", numero de habitacion= (" +auxHabitacion+ "), dni del pasajero= (" +auxPasajero+ ")]";
    }

    @Override
    public int hashCode(){
        return Long.hashCode(reservaId);
    }

    @Override
    public boolean equals(Object o){
        if (this == o) { return true; }
        if(o==null || o.getClass() != this.getClass()) { return false; }

        Reserva reserva = (Reserva) o;
        return reserva.getReservaId() ==  this.reservaId;
    }

    public long getReservaId() {
        return reservaId;
    }

    public void setReservaId(long reservaId) {
        this.reservaId = reservaId;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getCantidadPasajeros() {
        return cantidadPasajeros;
    }

    public void setCantidadPasajeros(int cantidadPasajeros) {
        this.cantidadPasajeros = cantidadPasajeros;
    }

    public EstadoReserva getEstadoReserva() {
        return estadoReserva;
    }

    public void setEstadoReserva(EstadoReserva estadoReserva) {
        this.estadoReserva = estadoReserva;
    }

    public int getDuracionEstadia() {
        return duracionEstadia;
    }

    public void setDuracionEstadia(int duracionEstadia) {
        this.duracionEstadia = duracionEstadia;
    }

    public LocalDateTime getDiaReservado() {
        return diaReservado;
    }

    public void setDiaReservado(LocalDateTime diaReservado) {
        this.diaReservado = diaReservado;
    }

    public LocalDateTime getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDateTime checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDateTime getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDateTime checkOut) {
        this.checkOut = checkOut;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
    }

    public Pasajero getPasajero() {
        return pasajero;
    }

    public void setPasajero(Pasajero pasajero) {
        this.pasajero = pasajero;
    }

    public int getNumeroHabitacion() {
        if (this.habitacion != null) {
            return this.habitacion.getNumeroHabitacion();
        }
        return 0;
    }

    public String getNombreCompletoPasajero() {
        if (this.pasajero != null) {
            return this.pasajero.getNombre() + " " + this.pasajero.getApellido();
        }
        return "Sin asignar";
    }

}
