package com.hotel.dao;

import com.hotel.config.ConexionDB;
import com.hotel.interfaces.IBajaLogica;
import com.hotel.interfaces.ICrud;
import com.hotel.interfaces.IListar;
import com.hotel.interfaces.IListarPorEstado;
import com.hotel.modelo.Habitacion;
import com.hotel.modelo.enums.EstadoHabitacion;
import com.hotel.modelo.enums.MotivoBloqueo;
import com.hotel.modelo.enums.TipoHabitacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Maneja las operaciones en la base de datos para las habitaciones. */
public class HabitacionDao implements ICrud<Habitacion>, IBajaLogica, IListar<Habitacion>, IListarPorEstado<Habitacion> {

    /** Consulta base que comparten todos los métodos de búsqueda y listado. */
    private static final String SQL_SELECT = "SELECT habitacionId, numeroHabitacion, tipoHabitacion, " +
            "estadoHabitacion, motivoBloqueo, activo FROM Habitacion";

    @Override
    public void agregar(Habitacion habitacion) throws SQLException {
        String sql = "INSERT INTO Habitacion (numeroHabitacion, tipoHabitacion, " +
                "estadoHabitacion, motivoBloqueo, activo) values(?, ?, ?, ?, ?)";

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, habitacion.getNumeroHabitacion());
            ps.setString(2, habitacion.getTipoHabitacion().name());
            ps.setString(3, habitacion.getEstadoHabitacion().name());
            setMotivoBloqueo(ps, 4, habitacion.getMotivoBloqueo());
            ps.setBoolean(5, habitacion.isActivo());
            ps.executeUpdate();

            // Le asigna a la habitación el ID que generó la base de datos
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    habitacion.setHabitacionId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public void modificar(Habitacion habitacion) throws SQLException {
        String sql = "UPDATE Habitacion SET numeroHabitacion = ?, tipoHabitacion = ?, " +
                "estadoHabitacion = ?, motivoBloqueo = ?, activo = ? where habitacionId = ?";

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, habitacion.getNumeroHabitacion());
            ps.setString(2, habitacion.getTipoHabitacion().name());
            ps.setString(3, habitacion.getEstadoHabitacion().name());
            setMotivoBloqueo(ps, 4, habitacion.getMotivoBloqueo());
            ps.setBoolean(5, habitacion.isActivo());
            ps.setLong(6, habitacion.getHabitacionId());
            ps.executeUpdate();
        }
    }

    @Override
    public void baja(long id) throws SQLException {
        String sql = "UPDATE Habitacion SET activo = false WHERE habitacionId = ?";

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void reactivar(long id) throws SQLException {
        String sql = "UPDATE Habitacion SET activo = true WHERE habitacionId = ?";

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Habitacion buscarPorId(long id) throws SQLException {
        String sql = SQL_SELECT + " WHERE habitacionId = ?";

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearHabitacion(rs);
                }
            }
        }

        return null; // Si no encontró ninguna habitación con ese ID
    }

    /** Busca una habitación por su número físico (ej: 101), o null si no existe. */
    public Habitacion buscarPorNumero(int numeroHabitacion) throws SQLException {
        String sql = SQL_SELECT + " WHERE numeroHabitacion = ?";

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, numeroHabitacion);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearHabitacion(rs);
                }
            }
        }

        return null; // Si no encontró ninguna habitación con ese número
    }

    @Override
    public List<Habitacion> listar() throws SQLException {
        return ejecutarListado(SQL_SELECT + " ORDER BY numeroHabitacion");
    }

    @Override
    public List<Habitacion> listarActivos() throws SQLException {
        return ejecutarListado(SQL_SELECT + " WHERE activo = true ORDER BY numeroHabitacion");
    }

    @Override
    public List<Habitacion> listarInactivos() throws SQLException {
        return ejecutarListado(SQL_SELECT + " WHERE activo = false ORDER BY numeroHabitacion");
    }

    /** Lista las habitaciones activas que se encuentran en el estado indicado (Disponible, Ocupada, etc.). */
    public List<Habitacion> listarPorEstado(EstadoHabitacion estado) throws SQLException {
        String sql = SQL_SELECT + " WHERE activo = true AND estadoHabitacion = ? ORDER BY numeroHabitacion";
        List<Habitacion> list = new ArrayList<>();

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, estado.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapearHabitacion(rs));
                }
            }
        }

        return list;
    }

    /**
     * Cambia solo el estado de una habitación (y su motivo de bloqueo).
     * El motivo puede ser null cuando la habitación no está bloqueada.
     */
    public void actualizarEstado(long id, EstadoHabitacion estado, MotivoBloqueo motivo) throws SQLException {
        String sql = "UPDATE Habitacion SET estadoHabitacion = ?, motivoBloqueo = ? WHERE habitacionId = ?";

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, estado.name());
            setMotivoBloqueo(ps, 2, motivo);
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    /** Ejecuta una consulta sin parámetros y arma la lista de habitaciones. */
    private List<Habitacion> ejecutarListado(String sql) throws SQLException {
        List<Habitacion> list = new ArrayList<>();

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapearHabitacion(rs));
                }
            }
        }

        return list;
    }

    /** Carga el motivo de bloqueo en el PreparedStatement, guardando NULL si no hay motivo. */
    private void setMotivoBloqueo(PreparedStatement ps, int indice, MotivoBloqueo motivo) throws SQLException {
        if (motivo == null) {
            ps.setNull(indice, Types.VARCHAR);
        } else {
            ps.setString(indice, motivo.name());
        }
    }

    /** Arma un objeto Habitacion a partir de una fila del ResultSet. */
    private Habitacion mapearHabitacion(ResultSet rs) throws SQLException {
        Habitacion habitacion = new Habitacion();
        habitacion.setHabitacionId(rs.getLong("habitacionId"));
        habitacion.setNumeroHabitacion(rs.getInt("numeroHabitacion"));
        habitacion.setTipoHabitacion(TipoHabitacion.valueOf(rs.getString("tipoHabitacion")));
        habitacion.setEstadoHabitacion(EstadoHabitacion.valueOf(rs.getString("estadoHabitacion")));

        // motivoBloqueo puede venir NULL de la base, y valueOf(null) lanzaría una excepción
        String motivo = rs.getString("motivoBloqueo");
        habitacion.setMotivoBloqueo(motivo == null ? null : MotivoBloqueo.valueOf(motivo));

        habitacion.setActivo(rs.getBoolean("activo"));
        return habitacion;
    }
}
