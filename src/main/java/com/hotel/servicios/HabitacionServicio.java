package com.hotel.servicios;

import com.hotel.dao.HabitacionDao;
import com.hotel.dao.ReservaDao;
import com.hotel.excepciones.HotelException;
import com.hotel.interfaces.IBajaLogica;
import com.hotel.interfaces.ICrud;
import com.hotel.interfaces.IListar;
import com.hotel.interfaces.IListarPorEstado;
import com.hotel.modelo.Habitacion;
import com.hotel.modelo.Reserva;
import com.hotel.modelo.enums.EstadoHabitacion;
import com.hotel.modelo.enums.EstadoReserva;
import com.hotel.modelo.enums.MotivoBloqueo;

import java.sql.SQLException;
import java.util.List;

/**
 * Lógica de negocio de las habitaciones.
 * Implementa las mismas interfaces que HabitacionDao, pero sus métodos no lanzan SQLException:
 * los errores se convierten en HotelException con un mensaje para mostrar en pantalla.
 *
 * Los estados OCUPADA y RESERVADA los maneja ReservaServicio (reservas, check-in y check-out);
 * desde acá solo se pasa una habitación entre DISPONIBLE y NO_DISPONIBLE (bloqueo).
 */
public class HabitacionServicio implements ICrud<Habitacion>, IBajaLogica, IListar<Habitacion>, IListarPorEstado<Habitacion> {

    private final HabitacionDao habitacionDao = new HabitacionDao();
    private final ReservaDao reservaDao = new ReservaDao();

    @Override
    public void agregar(Habitacion habitacion) {
        validarHabitacion(habitacion);

        // Una habitación nueva no puede tener reservas: solo puede empezar DISPONIBLE o bloqueada
        if (esEstadoDeReserva(habitacion.getEstadoHabitacion())) {
            throw new HotelException("Una habitación nueva solo puede estar DISPONIBLE o NO_DISPONIBLE.");
        }

        try {
            verificarNumeroDisponible(habitacion.getNumeroHabitacion(), 0);
            habitacionDao.agregar(habitacion);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("registrar la habitación", e);
        }
    }

    @Override
    public void modificar(Habitacion habitacion) {
        validarHabitacion(habitacion);

        try {
            Habitacion actual = obtenerExistente(habitacion.getHabitacionId());

            // La baja y la reactivación tienen sus propios métodos (con sus reglas):
            // modificar conserva el 'activo' que ya tiene la habitación en la base
            habitacion.setActivo(actual.isActivo());

            // OCUPADA y RESERVADA dependen de las reservas: no se cambian a mano desde acá
            boolean cambiaEstado = actual.getEstadoHabitacion() != habitacion.getEstadoHabitacion();
            if (cambiaEstado && (esEstadoDeReserva(actual.getEstadoHabitacion())
                    || esEstadoDeReserva(habitacion.getEstadoHabitacion()))) {
                throw new HotelException("Los estados OCUPADA y RESERVADA se manejan desde las reservas.");
            }

            verificarNumeroDisponible(habitacion.getNumeroHabitacion(), habitacion.getHabitacionId());
            habitacionDao.modificar(habitacion);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("modificar la habitación", e);
        }
    }

    /** Da de baja la habitación, siempre que no tenga reservas activas. */
    @Override
    public void baja(long id) {
        try {
            Habitacion habitacion = obtenerExistente(id);

            if (tieneReservasActivas(id)) {
                throw new HotelException("La habitación " + habitacion.getNumeroHabitacion()
                        + " tiene reservas activas: no se puede dar de baja.");
            }

            habitacionDao.baja(id);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("dar de baja la habitación", e);
        }
    }

    @Override
    public void reactivar(long id) {
        try {
            obtenerExistente(id);
            habitacionDao.reactivar(id);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("reactivar la habitación", e);
        }
    }

    /**
     * Bloquea una habitación por un motivo (limpieza, reparación, etc.).
     * No se puede bloquear una habitación ocupada o reservada.
     */
    public void bloquear(long id, MotivoBloqueo motivo) {
        if (motivo == null) {
            throw new HotelException("Debe indicar el motivo del bloqueo.");
        }

        try {
            Habitacion habitacion = obtenerExistente(id);

            if (!habitacion.isActivo()) {
                throw new HotelException("La habitación " + habitacion.getNumeroHabitacion() + " está dada de baja.");
            }
            if (esEstadoDeReserva(habitacion.getEstadoHabitacion())) {
                throw new HotelException("La habitación " + habitacion.getNumeroHabitacion()
                        + " está " + habitacion.getEstadoHabitacion() + ": no se puede bloquear.");
            }

            habitacionDao.actualizarEstado(id, EstadoHabitacion.NO_DISPONIBLE, motivo);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("bloquear la habitación", e);
        }
    }

    /** Libera una habitación bloqueada y la deja DISPONIBLE. */
    public void liberar(long id) {
        try {
            Habitacion habitacion = obtenerExistente(id);

            if (habitacion.getEstadoHabitacion() != EstadoHabitacion.NO_DISPONIBLE) {
                throw new HotelException("La habitación " + habitacion.getNumeroHabitacion() + " no está bloqueada.");
            }

            habitacionDao.actualizarEstado(id, EstadoHabitacion.DISPONIBLE, null);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("liberar la habitación", e);
        }
    }

    /** Devuelve la habitación con ese ID, o null si no existe. */
    @Override
    public Habitacion buscarPorId(long id) {
        try {
            return habitacionDao.buscarPorId(id);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("buscar la habitación", e);
        }
    }

    /** Devuelve la habitación con ese número (ej: 101), o null si no existe. */
    public Habitacion buscarPorNumero(int numeroHabitacion) {
        try {
            return habitacionDao.buscarPorNumero(numeroHabitacion);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("buscar la habitación", e);
        }
    }

    @Override
    public List<Habitacion> listar() {
        try {
            return habitacionDao.listar();
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("listar las habitaciones", e);
        }
    }

    @Override
    public List<Habitacion> listarActivos() {
        try {
            return habitacionDao.listarActivos();
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("listar las habitaciones activas", e);
        }
    }

    @Override
    public List<Habitacion> listarInactivos() {
        try {
            return habitacionDao.listarInactivos();
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("listar las habitaciones inactivas", e);
        }
    }

    /** Lista las habitaciones activas en el estado indicado (ej: las DISPONIBLES para reservar). */
    public List<Habitacion> listarPorEstado(EstadoHabitacion estado) {
        if (estado == null) {
            throw new HotelException("Debe indicar el estado de las habitaciones a listar.");
        }

        try {
            return habitacionDao.listarPorEstado(estado);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("listar las habitaciones", e);
        }
    }

    /** Valida los datos de la habitación y que el estado sea coherente con el motivo de bloqueo. */
    private void validarHabitacion(Habitacion habitacion) {
        if (habitacion == null) {
            throw new HotelException("Los datos de la habitación son obligatorios.");
        }
        if (habitacion.getNumeroHabitacion() <= 0) {
            throw new HotelException("El número de habitación debe ser mayor a 0.");
        }
        if (habitacion.getTipoHabitacion() == null) {
            throw new HotelException("Debe seleccionar el tipo de habitación.");
        }
        if (habitacion.getEstadoHabitacion() == null) {
            throw new HotelException("Debe seleccionar el estado de la habitación.");
        }

        // Solo una habitación NO_DISPONIBLE tiene motivo de bloqueo, y en ese caso es obligatorio
        boolean bloqueada = habitacion.getEstadoHabitacion() == EstadoHabitacion.NO_DISPONIBLE;
        if (bloqueada && habitacion.getMotivoBloqueo() == null) {
            throw new HotelException("Una habitación NO_DISPONIBLE debe tener un motivo de bloqueo.");
        }
        if (!bloqueada && habitacion.getMotivoBloqueo() != null) {
            throw new HotelException("Solo una habitación NO_DISPONIBLE puede tener motivo de bloqueo.");
        }
    }

    /** Verifica que el número no lo use otra habitación (idActual = 0 si es un alta nueva). */
    private void verificarNumeroDisponible(int numeroHabitacion, long idActual) throws SQLException {
        Habitacion existente = habitacionDao.buscarPorNumero(numeroHabitacion);
        if (existente != null && existente.getHabitacionId() != idActual) {
            throw new HotelException("Ya existe una habitación con el número " + numeroHabitacion + ".");
        }
    }

    /** Indica si la habitación tiene alguna reserva en estado ACTIVA. */
    private boolean tieneReservasActivas(long habitacionId) throws SQLException {
        for (Reserva reserva : reservaDao.listarPorHabitacion(habitacionId)) {
            if (reserva.getEstadoReserva() == EstadoReserva.ACTIVA) {
                return true;
            }
        }
        return false;
    }

    /** OCUPADA y RESERVADA son estados que dependen de una reserva. */
    private boolean esEstadoDeReserva(EstadoHabitacion estado) {
        return estado == EstadoHabitacion.OCUPADA || estado == EstadoHabitacion.RESERVADA;
    }

    /** Trae la habitación de la base, o lanza una excepción si no existe. */
    private Habitacion obtenerExistente(long id) throws SQLException {
        Habitacion habitacion = habitacionDao.buscarPorId(id);
        if (habitacion == null) {
            throw new HotelException("No existe una habitación con ID " + id + ".");
        }
        return habitacion;
    }
}
