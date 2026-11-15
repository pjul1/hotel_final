package com.hotel.servicios;

import com.hotel.dao.HabitacionDao;
import com.hotel.dao.ReservaDao;
import com.hotel.excepciones.HabitacionNoDisponible;
import com.hotel.excepciones.HotelException;
import com.hotel.interfaces.IBajaLogica;
import com.hotel.interfaces.ICrud;
import com.hotel.interfaces.IListar;
import com.hotel.interfaces.IListarPorEstado;
import com.hotel.modelo.Habitacion;
import com.hotel.modelo.enums.EstadoHabitacion;
import com.hotel.modelo.enums.MotivoBloqueo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lógica de negocio de las habitaciones.
 * Implementa las mismas interfaces que HabitacionDao, pero sus métodos no lanzan SQLException:
 * los errores se convierten en HotelException con un mensaje para mostrar en pantalla.
 * Mantiene un Set en memoria sincronizado con MySQL (Alternativa B) para resolver consultas
 * y filtros sin consultar continuamente la base de datos.
 *
 * Estados: desde acá se pasa una habitación entre DISPONIBLE y NO_DISPONIBLE (bloqueo).
 * OCUPADA la manejan el check-in y el check-out de ReservaServicio. RESERVADA depende de la fecha:
 * una habitación está RESERVADA solo mientras transcurre el período de una reserva sin check-in
 * (antes de que empiece sigue DISPONIBLE). Como la base no cambia sola, actualizarEstadosPorFecha()
 * se llama al iniciar sesión o al abrir la pantalla de habitaciones.
 *
 * Disponibilidad en un período: puedeOcupar() y listarDisponiblesEnPeriodo() consultan la ocupación
 * actual y las reservas ACTIVA (período = diaReservado + duracionEstadia días).
 */
public class HabitacionServicio implements ICrud<Habitacion>, IBajaLogica, IListar<Habitacion>, IListarPorEstado<Habitacion> {

    private static HabitacionServicio instancia;

    private final HabitacionDao habitacionDao = new HabitacionDao();
    private final ReservaDao reservaDao = new ReservaDao();

    /** Colección en memoria que garantiza unicidad de habitaciones y conserva el orden de carga. */
    private final Set<Habitacion> habitaciones = new LinkedHashSet<>();

    /** Carga inicial de las habitaciones desde MySQL hacia el Set en memoria. */
    private HabitacionServicio() {
        cargarDesdeBase();
    }

    public static HabitacionServicio getInstancia() {
        if (instancia == null) {
            instancia = new HabitacionServicio();
        }
        return instancia;
    }

    @Override
    public void agregar(Habitacion habitacion) {
        validarHabitacion(habitacion);

        // Una habitación nueva no puede tener reservas: solo puede empezar DISPONIBLE o bloqueada
        if (esEstadoDeReserva(habitacion.getEstadoHabitacion())) {
            throw new HotelException("Una habitación nueva solo puede estar DISPONIBLE o NO_DISPONIBLE.");
        }

        verificarNumeroDisponible(habitacion.getNumeroHabitacion(), 0);

        try {
            habitacionDao.agregar(habitacion);
            habitaciones.add(habitacion);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("registrar la habitación", e);
        }
    }

    @Override
    public void modificar(Habitacion habitacion) {
        validarHabitacion(habitacion);

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

        try {
            habitacionDao.modificar(habitacion);
            habitaciones.remove(actual);
            habitaciones.add(habitacion);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("modificar la habitación", e);
        }
    }

    /**
     * Da de baja la habitación, siempre que no tenga reservas pendientes (con el pasajero alojado
     * o con un período que todavía no terminó). Una reserva vencida sin check-in no la impide.
     */
    @Override
    public void baja(long id) {
        try {
            Habitacion habitacion = obtenerExistente(id);

            if (reservaDao.tieneReservasPendientes(id, LocalDate.now())) {
                throw new HotelException("La habitación " + habitacion.getNumeroHabitacion()
                        + " tiene reservas pendientes: no se puede dar de baja.");
            }

            habitacionDao.baja(id);
            habitacion.setActivo(false);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("dar de baja la habitación", e);
        }
    }

    @Override
    public void reactivar(long id) {
        try {
            Habitacion habitacion = obtenerExistente(id);
            habitacionDao.reactivar(id);
            habitacion.setActivo(true);
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
            habitacion.setEstadoHabitacion(EstadoHabitacion.NO_DISPONIBLE);
            habitacion.setMotivoBloqueo(motivo);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("bloquear la habitación", e);
        }
    }

    /**
     * Libera una habitación bloqueada y la deja DISPONIBLE
     * (o RESERVADA, si hoy transcurre el período de una de sus reservas).
     */
    public void liberar(long id) {
        try {
            Habitacion habitacion = obtenerExistente(id);

            if (habitacion.getEstadoHabitacion() != EstadoHabitacion.NO_DISPONIBLE) {
                throw new HotelException("La habitación " + habitacion.getNumeroHabitacion() + " no está bloqueada.");
            }

            habitacionDao.actualizarEstado(id, EstadoHabitacion.DISPONIBLE, null);
            habitacionDao.marcarReservadasPorFecha(LocalDate.now());
            cargarDesdeBase();
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("liberar la habitación", e);
        }
    }

    /**
     * Pone al día los estados según la fecha de hoy: pasa a RESERVADA las habitaciones cuyo período
     * de reserva está transcurriendo (sin check-in) y devuelve a DISPONIBLE las que ya no lo tienen.
     * No toca las OCUPADA ni las NO_DISPONIBLE, y nunca cambia el estado de las reservas.
     *
     * Cuándo se llama (la base no cambia sola al pasar los días):
     * - PENDIENTE en las vistas: al iniciar sesión o al abrir la pantalla de habitaciones, antes de listarlas.
     * - Automáticamente desde ReservaServicio, después de agregar, modificar y checkOut, porque esas
     *   acciones pueden cambiar qué habitación está reservada hoy.
     */
    public void actualizarEstadosPorFecha() {
        try {
            LocalDate hoy = LocalDate.now();
            habitacionDao.liberarReservadasSinReservaVigente(hoy);
            habitacionDao.marcarReservadasPorFecha(hoy);
            cargarDesdeBase();
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("actualizar los estados de las habitaciones", e);
        }
    }

    /**
     * Informa si es posible ocupar la habitación durante 'dias' días a partir de 'desde',
     * consultando su estado actual y las reservas ACTIVA de ese período.
     */
    public boolean puedeOcupar(long habitacionId, LocalDate desde, int dias) {
        validarPeriodo(desde, dias);

        try {
            return motivoNoDisponible(obtenerExistente(habitacionId), desde, dias, 0) == null;
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("consultar la disponibilidad", e);
        }
    }

    /** Lista las habitaciones activas que se pueden ocupar durante 'dias' días a partir de 'desde'. */
    public List<Habitacion> listarDisponiblesEnPeriodo(LocalDate desde, int dias) {
        validarPeriodo(desde, dias);
        List<Habitacion> list = new ArrayList<>();

        try {
            for (Habitacion habitacion : listarActivos()) {
                if (motivoNoDisponible(habitacion, desde, dias, 0) == null) {
                    list.add(habitacion);
                }
            }
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("listar las habitaciones disponibles", e);
        }

        return list;
    }

    /**
     * Verifica que la habitación se pueda ocupar en el período, o lanza HabitacionNoDisponible con el motivo.
     * La usa ReservaServicio al crear o modificar una reserva (por eso no es pública).
     *
     * @param reservaIdExcluida reserva que se está modificando (para que no choque consigo misma); 0 si es nueva
     */
    void verificarDisponibilidad(Habitacion habitacion, LocalDate desde, int dias, long reservaIdExcluida)
            throws SQLException {
        String motivo = motivoNoDisponible(habitacion, desde, dias, reservaIdExcluida);
        if (motivo != null) {
            throw new HabitacionNoDisponible(motivo);
        }
    }

    /** Devuelve la habitación con ese ID desde el Set en memoria, o null si no existe. */
    @Override
    public Habitacion buscarPorId(long id) {
        for (Habitacion habitacion : habitaciones) {
            if (habitacion.getHabitacionId() == id) {
                return habitacion;
            }
        }
        return null;
    }

    /** Devuelve la habitación con ese número (ej: 101) desde el Set en memoria, o null si no existe. */
    public Habitacion buscarPorNumero(int numeroHabitacion) {
        for (Habitacion habitacion : habitaciones) {
            if (habitacion.getNumeroHabitacion() == numeroHabitacion) {
                return habitacion;
            }
        }
        return null;
    }

    @Override
    public List<Habitacion> listar() {
        return new ArrayList<>(habitaciones);
    }

    @Override
    public List<Habitacion> listarActivos() {
        List<Habitacion> activos = new ArrayList<>();
        for (Habitacion habitacion : habitaciones) {
            if (habitacion.isActivo()) {
                activos.add(habitacion);
            }
        }
        return activos;
    }

    @Override
    public List<Habitacion> listarInactivos() {
        List<Habitacion> inactivos = new ArrayList<>();
        for (Habitacion habitacion : habitaciones) {
            if (!habitacion.isActivo()) {
                inactivos.add(habitacion);
            }
        }
        return inactivos;
    }

    /** Lista las habitaciones activas en el estado indicado (ej: las DISPONIBLES para reservar). */
    public List<Habitacion> listarPorEstado(EstadoHabitacion estado) {
        if (estado == null) {
            throw new HotelException("Debe indicar el estado de las habitaciones a listar.");
        }

        List<Habitacion> porEstado = new ArrayList<>();
        for (Habitacion habitacion : habitaciones) {
            if (habitacion.isActivo() && habitacion.getEstadoHabitacion() == estado) {
                porEstado.add(habitacion);
            }
        }
        return porEstado;
    }

    /** Carga inicial desde MySQL al Set en memoria. */
    private void cargarDesdeBase() {
        try {
            habitaciones.clear();
            habitaciones.addAll(habitacionDao.listar());
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("cargar las habitaciones", e);
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

    /** Verifica en memoria que el número no lo use otra habitación (idActual = 0 si es un alta nueva). */
    private void verificarNumeroDisponible(int numeroHabitacion, long idActual) {
        Habitacion existente = buscarPorNumero(numeroHabitacion);
        if (existente != null && existente.getHabitacionId() != idActual) {
            throw new HotelException("Ya existe una habitación con el número " + numeroHabitacion + ".");
        }
    }

    /** Valida los datos de un período consultado: fecha de inicio desde hoy y al menos 1 día. */
    private void validarPeriodo(LocalDate desde, int dias) {
        if (desde == null) {
            throw new HotelException("Debe indicar la fecha de inicio del período.");
        }
        if (desde.isBefore(LocalDate.now())) {
            throw new HotelException("La fecha de inicio no puede ser anterior a hoy.");
        }
        if (dias <= 0) {
            throw new HotelException("El período debe ser de al menos 1 día.");
        }
    }

    /**
     * Devuelve por qué la habitación no se puede ocupar en el período, o null si se puede.
     * Una habitación bloqueada no se ofrece (el bloqueo no tiene fecha de fin), una OCUPADA no se
     * ofrece para hoy (el pasajero sigue alojado) y no puede haber otra reserva ACTIVA en esos días.
     */
    private String motivoNoDisponible(Habitacion habitacion, LocalDate desde, int dias, long reservaIdExcluida)
            throws SQLException {
        if (habitacion == null) {
            return "La habitación seleccionada no existe.";
        }
        if (!habitacion.isActivo()) {
            return "La habitación " + habitacion.getNumeroHabitacion() + " está dada de baja.";
        }
        if (habitacion.getEstadoHabitacion() == EstadoHabitacion.NO_DISPONIBLE) {
            return "La habitación " + habitacion.getNumeroHabitacion()
                    + " está bloqueada por " + habitacion.getMotivoBloqueo() + ".";
        }
        if (habitacion.getEstadoHabitacion() == EstadoHabitacion.OCUPADA && !desde.isAfter(LocalDate.now())) {
            return "La habitación " + habitacion.getNumeroHabitacion() + " está ocupada hoy.";
        }
        if (reservaDao.existeReservaEnPeriodo(habitacion.getHabitacionId(), desde, desde.plusDays(dias),
                reservaIdExcluida)) {
            return "La habitación " + habitacion.getNumeroHabitacion() + " ya está reservada en ese período.";
        }
        return null;
    }

    /** OCUPADA y RESERVADA son estados que dependen de una reserva. */
    private boolean esEstadoDeReserva(EstadoHabitacion estado) {
        return estado == EstadoHabitacion.OCUPADA || estado == EstadoHabitacion.RESERVADA;
    }

    /** Trae la habitación desde el Set en memoria, o lanza una excepción si no existe. */
    private Habitacion obtenerExistente(long id) {
        Habitacion habitacion = buscarPorId(id);
        if (habitacion == null) {
            throw new HotelException("No existe una habitación con ID " + id + ".");
        }
        return habitacion;
    }
}
