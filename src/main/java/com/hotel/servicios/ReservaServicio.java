package com.hotel.servicios;

import com.hotel.dao.HabitacionDao;
import com.hotel.dao.PasajeroDao;
import com.hotel.dao.ReservaDao;
import com.hotel.excepciones.HabitacionNoDisponible;
import com.hotel.excepciones.HotelException;
import com.hotel.excepciones.ReservaInvalidaException;
import com.hotel.interfaces.ICrud;
import com.hotel.interfaces.IListar;
import com.hotel.modelo.Habitacion;
import com.hotel.modelo.Pasajero;
import com.hotel.modelo.Reserva;
import com.hotel.modelo.enums.EstadoHabitacion;
import com.hotel.modelo.enums.EstadoReserva;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio de las reservas: alta, modificación, cancelación, check-in y check-out.
 * Implementa las mismas interfaces que ReservaDao (ICrud e IListar; no tiene baja lógica:
 * una reserva se cancela). Sus métodos no lanzan SQLException, sino HotelException o sus subclases.
 *
 * Disponibilidad: la base todavía no guarda la fecha de ingreso de la reserva, así que no se pueden
 * comparar períodos. Por ahora, una habitación con una reserva ACTIVA no se puede volver a reservar
 * hasta que esa reserva termine (check-out) o se cancele.
 */
public class ReservaServicio implements ICrud<Reserva>, IListar<Reserva> {

    /** Máximo que admite la columna cantidadPasajeros (TINYINT UNSIGNED). */
    private static final int MAXIMO_PASAJEROS = 255;

    private final ReservaDao reservaDao = new ReservaDao();
    private final HabitacionDao habitacionDao = new HabitacionDao();
    private final PasajeroDao pasajeroDao = new PasajeroDao();

    /**
     * Registra una nueva reserva y marca la habitación como RESERVADA.
     * La reserva siempre se crea ACTIVA y sin check-in ni check-out.
     */
    @Override
    public void agregar(Reserva reserva) {
        validarReserva(reserva);

        try {
            Habitacion habitacion = habitacionDao.buscarPorId(reserva.getHabitacion().getHabitacionId());
            verificarHabitacionDisponible(habitacion, 0);
            Pasajero pasajero = obtenerPasajeroActivo(reserva.getPasajero().getUsuarioId());

            // Se usan los datos actuales de la base, no los que vinieron de la pantalla
            reserva.setHabitacion(habitacion);
            reserva.setPasajero(pasajero);
            reserva.setEstadoReserva(EstadoReserva.ACTIVA);
            reserva.setCheckIn(null);
            reserva.setCheckOut(null);
            if (reserva.getDiaReservado() == null) {
                reserva.setDiaReservado(LocalDateTime.now());
            }

            reservaDao.agregar(reserva);
            habitacionDao.actualizarEstado(habitacion.getHabitacionId(), EstadoHabitacion.RESERVADA, null);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("registrar la reserva", e);
        }
    }

    /**
     * Modifica una reserva ACTIVA que todavía no hizo check-in.
     * El estado y las fechas no se cambian acá: tienen sus propios métodos (cancelar, checkIn, checkOut).
     */
    @Override
    public void modificar(Reserva reserva) {
        validarReserva(reserva);

        try {
            Reserva actual = obtenerExistente(reserva.getReservaId());
            verificarPendiente(actual, "modificar");

            Habitacion habitacionAnterior = actual.getHabitacion();
            Habitacion habitacionNueva = habitacionDao.buscarPorId(reserva.getHabitacion().getHabitacionId());
            boolean cambiaHabitacion = habitacionAnterior.getHabitacionId() != reserva.getHabitacion().getHabitacionId();
            if (cambiaHabitacion) {
                verificarHabitacionDisponible(habitacionNueva, reserva.getReservaId());
            }
            Pasajero pasajero = obtenerPasajeroActivo(reserva.getPasajero().getUsuarioId());

            reserva.setHabitacion(habitacionNueva);
            reserva.setPasajero(pasajero);
            reserva.setEstadoReserva(actual.getEstadoReserva());
            reserva.setDiaReservado(actual.getDiaReservado());
            reserva.setCheckIn(null);
            reserva.setCheckOut(null);

            reservaDao.modificar(reserva);

            // Si cambió de habitación, se libera la anterior y se reserva la nueva
            if (cambiaHabitacion) {
                liberarSiEstaReservada(habitacionAnterior);
                habitacionDao.actualizarEstado(habitacionNueva.getHabitacionId(), EstadoHabitacion.RESERVADA, null);
            }
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("modificar la reserva", e);
        }
    }

    /** Cancela una reserva ACTIVA que todavía no hizo check-in, y libera su habitación. */
    public void cancelar(long reservaId) {
        try {
            Reserva reserva = obtenerExistente(reservaId);
            verificarPendiente(reserva, "cancelar");

            reservaDao.cambiarEstado(reservaId, EstadoReserva.CANCELADA);
            liberarSiEstaReservada(reserva.getHabitacion());
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("cancelar la reserva", e);
        }
    }

    /** Registra la llegada del pasajero (ahora mismo) y marca la habitación como OCUPADA. */
    public void checkIn(long reservaId) {
        try {
            Reserva reserva = obtenerExistente(reservaId);
            verificarPendiente(reserva, "hacer el check-in de");

            Habitacion habitacion = reserva.getHabitacion();
            if (!habitacion.isActivo() || habitacion.getEstadoHabitacion() == EstadoHabitacion.NO_DISPONIBLE) {
                throw new HabitacionNoDisponible("La habitación " + habitacion.getNumeroHabitacion()
                        + " no está disponible para el check-in.");
            }

            reservaDao.registrarCheckIn(reservaId, LocalDateTime.now());
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("registrar el check-in", e);
        }
    }

    /** Registra la salida del pasajero (ahora mismo), cumple la reserva y deja la habitación DISPONIBLE. */
    public void checkOut(long reservaId) {
        try {
            Reserva reserva = obtenerExistente(reservaId);

            if (reserva.getEstadoReserva() != EstadoReserva.ACTIVA) {
                throw new ReservaInvalidaException("La reserva " + reservaId + " no está activa.");
            }
            if (reserva.getCheckIn() == null) {
                throw new ReservaInvalidaException("La reserva " + reservaId + " todavía no hizo el check-in.");
            }

            reservaDao.registrarCheckOut(reservaId, LocalDateTime.now());
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("registrar el check-out", e);
        }
    }

    /** Devuelve la reserva con ese ID, o null si no existe. */
    @Override
    public Reserva buscarPorId(long id) {
        try {
            return reservaDao.buscarPorId(id);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("buscar la reserva", e);
        }
    }

    @Override
    public List<Reserva> listar() {
        try {
            return reservaDao.listar();
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("listar las reservas", e);
        }
    }

    /** Lista todas las reservas de un pasajero (historial y reservas activas). */
    public List<Reserva> listarPorPasajero(long pasajeroId) {
        try {
            return reservaDao.listarPorPasajero(pasajeroId);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("listar las reservas del pasajero", e);
        }
    }

    /** Lista todas las reservas de una habitación. */
    public List<Reserva> listarPorHabitacion(long habitacionId) {
        try {
            return reservaDao.listarPorHabitacion(habitacionId);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("listar las reservas de la habitación", e);
        }
    }

    /** Lista las reservas en el estado indicado (Activa, Cumplida, Cancelada). */
    public List<Reserva> listarPorEstado(EstadoReserva estado) {
        if (estado == null) {
            throw new HotelException("Debe indicar el estado de las reservas a listar.");
        }

        try {
            return reservaDao.listarPorEstado(estado);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("listar las reservas", e);
        }
    }

    /** Valida los datos que se cargan al crear o modificar una reserva. */
    private void validarReserva(Reserva reserva) {
        if (reserva == null) {
            throw new ReservaInvalidaException("Los datos de la reserva son obligatorios.");
        }
        if (reserva.getHabitacion() == null) {
            throw new ReservaInvalidaException("Debe seleccionar una habitación.");
        }
        if (reserva.getPasajero() == null) {
            throw new ReservaInvalidaException("Debe seleccionar un pasajero.");
        }
        if (reserva.getPrecio() <= 0) {
            throw new ReservaInvalidaException("El precio debe ser mayor a 0.");
        }
        if (reserva.getCantidadPasajeros() <= 0) {
            throw new ReservaInvalidaException("La cantidad de pasajeros debe ser mayor a 0.");
        }
        if (reserva.getCantidadPasajeros() > MAXIMO_PASAJEROS) {
            throw new ReservaInvalidaException("La cantidad de pasajeros no puede superar " + MAXIMO_PASAJEROS + ".");
        }
        if (reserva.getDuracionEstadia() <= 0) {
            throw new ReservaInvalidaException("La duración de la estadía debe ser de al menos 1 día.");
        }
    }

    /**
     * Verifica que la habitación exista, esté activa, no esté bloqueada
     * y no tenga otra reserva ACTIVA (reservaIdActual = 0 si es una reserva nueva).
     */
    private void verificarHabitacionDisponible(Habitacion habitacion, long reservaIdActual) throws SQLException {
        if (habitacion == null) {
            throw new HabitacionNoDisponible("La habitación seleccionada no existe.");
        }
        if (!habitacion.isActivo()) {
            throw new HabitacionNoDisponible("La habitación " + habitacion.getNumeroHabitacion() + " está dada de baja.");
        }
        if (habitacion.getEstadoHabitacion() == EstadoHabitacion.NO_DISPONIBLE) {
            throw new HabitacionNoDisponible("La habitación " + habitacion.getNumeroHabitacion()
                    + " está bloqueada por " + habitacion.getMotivoBloqueo() + ".");
        }

        for (Reserva otra : reservaDao.listarPorHabitacion(habitacion.getHabitacionId())) {
            if (otra.getEstadoReserva() == EstadoReserva.ACTIVA && otra.getReservaId() != reservaIdActual) {
                throw new HabitacionNoDisponible("La habitación " + habitacion.getNumeroHabitacion()
                        + " ya tiene una reserva activa.");
            }
        }
    }

    /** Trae el pasajero de la base y verifica que exista y esté activo. */
    private Pasajero obtenerPasajeroActivo(long pasajeroId) throws SQLException {
        Pasajero pasajero = pasajeroDao.buscarPorId(pasajeroId);
        if (pasajero == null) {
            throw new ReservaInvalidaException("El pasajero seleccionado no existe.");
        }
        if (!pasajero.isActivo()) {
            throw new ReservaInvalidaException("El pasajero " + pasajero.getNombre() + " "
                    + pasajero.getApellido() + " está dado de baja.");
        }
        return pasajero;
    }

    /** Verifica que la reserva esté ACTIVA y sin check-in (todavía se puede modificar o cancelar). */
    private void verificarPendiente(Reserva reserva, String accion) {
        if (reserva.getEstadoReserva() != EstadoReserva.ACTIVA) {
            throw new ReservaInvalidaException("No se puede " + accion + " la reserva " + reserva.getReservaId()
                    + ": está " + reserva.getEstadoReserva() + ".");
        }
        if (reserva.getCheckIn() != null) {
            throw new ReservaInvalidaException("No se puede " + accion + " la reserva " + reserva.getReservaId()
                    + ": el pasajero ya hizo el check-in.");
        }
    }

    /** Deja DISPONIBLE una habitación solo si estaba RESERVADA (no pisa un bloqueo ni una ocupación). */
    private void liberarSiEstaReservada(Habitacion habitacion) throws SQLException {
        if (habitacion.getEstadoHabitacion() == EstadoHabitacion.RESERVADA) {
            habitacionDao.actualizarEstado(habitacion.getHabitacionId(), EstadoHabitacion.DISPONIBLE, null);
        }
    }

    /** Trae la reserva de la base, o lanza una excepción si no existe. */
    private Reserva obtenerExistente(long id) throws SQLException {
        Reserva reserva = reservaDao.buscarPorId(id);
        if (reserva == null) {
            throw new ReservaInvalidaException("No existe una reserva con ID " + id + ".");
        }
        return reserva;
    }
}
