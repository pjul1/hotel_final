package com.hotel.dao;

import com.hotel.config.ConexionDB;
import com.hotel.interfaces.ICrud;
import com.hotel.interfaces.IListar;
import com.hotel.modelo.Habitacion;
import com.hotel.modelo.Pasajero;
import com.hotel.modelo.Reserva;
import com.hotel.modelo.enums.EstadoHabitacion;
import com.hotel.modelo.enums.EstadoReserva;
import com.hotel.modelo.enums.MotivoBloqueo;
import com.hotel.modelo.enums.TipoHabitacion;
import com.hotel.modelo.enums.TipoUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Maneja las operaciones en la base de datos para las reservas.
 * No implementa IBajaLogica ni IListarPorEstado porque la tabla Reserva no tiene campo 'activo':
 * una reserva se "da de baja" cambiando su estado a CANCELADA (ver cambiarEstado).
 */
public class ReservaDao implements ICrud<Reserva>, IListar<Reserva> {

    /**
     * Consulta base: une la reserva con su habitación y su pasajero para armar el objeto completo.
     * Habitacion y Usuario tienen ambos una columna 'activo', por eso se renombran con alias.
     */
    private static final String SQL_SELECT = "SELECT r.reservaId, r.precio, r.cantidadPasajeros, r.estadoReserva, " +
            "r.duracionEstadia, r.diaReservado, r.checkIn, r.checkOut, " +
            "h.habitacionId, h.numeroHabitacion, h.tipoHabitacion, h.estadoHabitacion, h.motivoBloqueo, " +
            "h.activo AS habitacionActiva, " +
            "u.usuarioId, u.nombre, u.apellido, u.email, u.dni, u.telefono, u.nombreUsuario, u.contrasenia, " +
            "u.activo AS usuarioActivo, u.tipoUsuario, p.origen, p.domicilioOrigen " +
            "FROM Reserva AS r " +
            "INNER JOIN Habitacion AS h ON r.habitacion = h.habitacionId " +
            "INNER JOIN Pasajero AS p ON r.pasajero = p.usuarioId " +
            "INNER JOIN Usuario AS u ON p.usuarioId = u.usuarioId";

    @Override
    public void agregar(Reserva reserva) throws SQLException {
        String sql = "INSERT INTO Reserva (precio, cantidadPasajeros, estadoReserva, duracionEstadia, " +
                "diaReservado, checkIn, checkOut, habitacion, pasajero) values(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setDouble(1, reserva.getPrecio());
            ps.setInt(2, reserva.getCantidadPasajeros());
            ps.setString(3, reserva.getEstadoReserva().name());
            ps.setInt(4, reserva.getDuracionEstadia());
            setFecha(ps, 5, reserva.getDiaReservado());
            setFecha(ps, 6, reserva.getCheckIn());
            setFecha(ps, 7, reserva.getCheckOut());
            ps.setLong(8, reserva.getHabitacion().getHabitacionId());
            ps.setLong(9, reserva.getPasajero().getUsuarioId());
            ps.executeUpdate();

            // Le asigna a la reserva el ID que generó la base de datos
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    reserva.setReservaId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public void modificar(Reserva reserva) throws SQLException {
        String sql = "UPDATE Reserva SET precio = ?, cantidadPasajeros = ?, estadoReserva = ?, " +
                "duracionEstadia = ?, diaReservado = ?, checkIn = ?, checkOut = ?, " +
                "habitacion = ?, pasajero = ? where reservaId = ?";

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setDouble(1, reserva.getPrecio());
            ps.setInt(2, reserva.getCantidadPasajeros());
            ps.setString(3, reserva.getEstadoReserva().name());
            ps.setInt(4, reserva.getDuracionEstadia());
            setFecha(ps, 5, reserva.getDiaReservado());
            setFecha(ps, 6, reserva.getCheckIn());
            setFecha(ps, 7, reserva.getCheckOut());
            ps.setLong(8, reserva.getHabitacion().getHabitacionId());
            ps.setLong(9, reserva.getPasajero().getUsuarioId());
            ps.setLong(10, reserva.getReservaId());
            ps.executeUpdate();
        }
    }

    @Override
    public Reserva buscarPorId(long id) throws SQLException {
        String sql = SQL_SELECT + " WHERE r.reservaId = ?";

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearReserva(rs);
                }
            }
        }

        return null; // Si no encontró ninguna reserva con ese ID
    }

    @Override
    public List<Reserva> listar() throws SQLException {
        String sql = SQL_SELECT + " ORDER BY r.diaReservado DESC";
        List<Reserva> list = new ArrayList<>();

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapearReserva(rs));
                }
            }
        }

        return list;
    }

    /** Lista todas las reservas de un pasajero (historial y reservas activas). */
    public List<Reserva> listarPorPasajero(long pasajeroId) throws SQLException {
        String sql = SQL_SELECT + " WHERE r.pasajero = ? ORDER BY r.diaReservado DESC";
        List<Reserva> list = new ArrayList<>();

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, pasajeroId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapearReserva(rs));
                }
            }
        }

        return list;
    }

    /** Lista todas las reservas de una habitación (sirve para controlar su disponibilidad). */
    public List<Reserva> listarPorHabitacion(long habitacionId) throws SQLException {
        String sql = SQL_SELECT + " WHERE r.habitacion = ? ORDER BY r.diaReservado DESC";
        List<Reserva> list = new ArrayList<>();

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, habitacionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapearReserva(rs));
                }
            }
        }

        return list;
    }

    /** Lista las reservas que se encuentran en el estado indicado (Activa, Cumplida, Cancelada). */
    public List<Reserva> listarPorEstado(EstadoReserva estado) throws SQLException {
        String sql = SQL_SELECT + " WHERE r.estadoReserva = ? ORDER BY r.diaReservado DESC";
        List<Reserva> list = new ArrayList<>();

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, estado.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapearReserva(rs));
                }
            }
        }

        return list;
    }

    /** Cambia solo el estado de una reserva (por ejemplo, para cancelarla). */
    public void cambiarEstado(long id, EstadoReserva estado) throws SQLException {
        String sql = "UPDATE Reserva SET estadoReserva = ? WHERE reservaId = ?";

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, estado.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Indica si la habitación tiene otra reserva ACTIVA cuyo período se superpone con [desde, hasta).
     * El período de una reserva va desde diaReservado hasta diaReservado + duracionEstadia días
     * (el check-in no cuenta: si el pasajero llega tarde, los días anteriores siguen reservados).
     * Se compara por día, por eso una reserva puede empezar el mismo día que termina otra.
     *
     * @param reservaIdExcluida reserva que no se tiene en cuenta (al modificar, para que no choque consigo
     *                          misma); 0 si no se excluye ninguna
     */
    public boolean existeReservaEnPeriodo(long habitacionId, LocalDate desde, LocalDate hasta,
                                          long reservaIdExcluida) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Reserva WHERE habitacion = ? AND estadoReserva = 'ACTIVA' " +
                "AND reservaId <> ? AND DATE(diaReservado) < ? " +
                "AND DATE_ADD(DATE(diaReservado), INTERVAL duracionEstadia DAY) > ?";

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, habitacionId);
            ps.setLong(2, reservaIdExcluida);
            ps.setObject(3, hasta);
            ps.setObject(4, desde);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Indica si la habitación tiene reservas ACTIVA pendientes al día indicado: con el pasajero
     * alojado (check-in hecho) o con un período que todavía no terminó.
     * Una reserva vencida sin check-in (el pasajero nunca llegó) no cuenta.
     */
    public boolean tieneReservasPendientes(long habitacionId, LocalDate hoy) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Reserva WHERE habitacion = ? AND estadoReserva = 'ACTIVA' " +
                "AND (checkIn IS NOT NULL OR DATE_ADD(DATE(diaReservado), INTERVAL duracionEstadia DAY) > ?)";

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, habitacionId);
            ps.setObject(2, hoy);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Registra el check-in: guarda la fecha de llegada en la reserva y marca su habitación como OCUPADA.
     * Hacemos dos comandos SQL en una misma transacción para que no quede uno hecho y el otro no.
     */
    public void registrarCheckIn(long reservaId, LocalDateTime fechaCheckIn) throws SQLException {
        String sqlReserva = "UPDATE Reserva SET checkIn = ? WHERE reservaId = ?";
        String sqlHabitacion = "UPDATE Habitacion AS h INNER JOIN Reserva AS r ON r.habitacion = h.habitacionId " +
                "SET h.estadoHabitacion = 'OCUPADA', h.motivoBloqueo = NULL WHERE r.reservaId = ?";

        try (Connection connection = ConexionDB.getConexion()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sqlReserva);
                 PreparedStatement psHabitacion = connection.prepareStatement(sqlHabitacion)) {

                setFecha(ps, 1, fechaCheckIn);
                ps.setLong(2, reservaId);
                ps.executeUpdate();

                psHabitacion.setLong(1, reservaId);
                psHabitacion.executeUpdate();

                connection.commit();

            } catch (SQLException e) {
                // Si algo salió mal en el medio, cancela todo para no dejar datos a medias
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /**
     * Registra el check-out: guarda la fecha de salida, pasa la reserva a CUMPLIDA
     * y deja su habitación DISPONIBLE, todo en una misma transacción.
     */
    public void registrarCheckOut(long reservaId, LocalDateTime fechaCheckOut) throws SQLException {
        String sqlReserva = "UPDATE Reserva SET checkOut = ?, estadoReserva = 'CUMPLIDA' WHERE reservaId = ?";
        String sqlHabitacion = "UPDATE Habitacion AS h INNER JOIN Reserva AS r ON r.habitacion = h.habitacionId " +
                "SET h.estadoHabitacion = 'DISPONIBLE', h.motivoBloqueo = NULL WHERE r.reservaId = ?";

        try (Connection connection = ConexionDB.getConexion()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sqlReserva);
                 PreparedStatement psHabitacion = connection.prepareStatement(sqlHabitacion)) {

                setFecha(ps, 1, fechaCheckOut);
                ps.setLong(2, reservaId);
                ps.executeUpdate();

                psHabitacion.setLong(1, reservaId);
                psHabitacion.executeUpdate();

                connection.commit();

            } catch (SQLException e) {
                // Si algo salió mal en el medio, cancela todo para no dejar datos a medias
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /** Carga una fecha en el PreparedStatement, guardando NULL si todavía no existe (ej: check-in pendiente). */
    private void setFecha(PreparedStatement ps, int indice, LocalDateTime fecha) throws SQLException {
        if (fecha == null) {
            ps.setNull(indice, Types.TIMESTAMP);
        } else {
            ps.setObject(indice, fecha);
        }
    }

    /** Arma un objeto Reserva (con su Habitacion y su Pasajero) a partir de una fila del ResultSet. */
    private Reserva mapearReserva(ResultSet rs) throws SQLException {
        Habitacion habitacion = new Habitacion();
        habitacion.setHabitacionId(rs.getLong("habitacionId"));
        habitacion.setNumeroHabitacion(rs.getInt("numeroHabitacion"));
        habitacion.setTipoHabitacion(TipoHabitacion.valueOf(rs.getString("tipoHabitacion")));
        habitacion.setEstadoHabitacion(EstadoHabitacion.valueOf(rs.getString("estadoHabitacion")));
        String motivo = rs.getString("motivoBloqueo"); // Puede venir NULL
        habitacion.setMotivoBloqueo(motivo == null ? null : MotivoBloqueo.valueOf(motivo));
        habitacion.setActivo(rs.getBoolean("habitacionActiva"));

        Pasajero pasajero = new Pasajero();
        pasajero.setUsuarioId(rs.getLong("usuarioId"));
        pasajero.setNombre(rs.getString("nombre"));
        pasajero.setApellido(rs.getString("apellido"));
        pasajero.setEmail(rs.getString("email"));
        pasajero.setDni(rs.getString("dni"));
        pasajero.setTelefono(rs.getString("telefono"));
        pasajero.setNombreUsuario(rs.getString("nombreUsuario"));
        pasajero.setContrasenia(rs.getString("contrasenia"));
        pasajero.setActivo(rs.getBoolean("usuarioActivo"));
        pasajero.setTipoUsuario(TipoUsuario.valueOf(rs.getString("tipoUsuario")));
        pasajero.setOrigen(rs.getString("origen"));
        pasajero.setDomicilioOrigen(rs.getString("domicilioOrigen"));

        Reserva reserva = new Reserva();
        reserva.setReservaId(rs.getLong("reservaId"));
        reserva.setPrecio(rs.getDouble("precio"));
        reserva.setCantidadPasajeros(rs.getInt("cantidadPasajeros"));
        reserva.setEstadoReserva(EstadoReserva.valueOf(rs.getString("estadoReserva")));
        reserva.setDuracionEstadia(rs.getInt("duracionEstadia"));
        // getObject con LocalDateTime.class devuelve null si la columna está vacía (checkIn/checkOut)
        reserva.setDiaReservado(rs.getObject("diaReservado", LocalDateTime.class));
        reserva.setCheckIn(rs.getObject("checkIn", LocalDateTime.class));
        reserva.setCheckOut(rs.getObject("checkOut", LocalDateTime.class));
        reserva.setHabitacion(habitacion);
        reserva.setPasajero(pasajero);
        return reserva;
    }
}
