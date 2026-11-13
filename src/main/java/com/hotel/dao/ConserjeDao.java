package com.hotel.dao;

import com.hotel.config.ConexionDB;
import com.hotel.interfaces.IBajaLogica;
import com.hotel.interfaces.ICrud;
import com.hotel.interfaces.IListar;
import com.hotel.interfaces.IListarPorEstado;
import com.hotel.modelo.Conserje;
import com.hotel.modelo.enums.TipoUsuario;
import com.hotel.modelo.enums.Turno;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Maneja las operaciones en la base de datos para los conserjes.*/
public class ConserjeDao implements ICrud<Conserje>, IBajaLogica, IListar<Conserje>, IListarPorEstado<Conserje> {

    /** Hacemos dos comandos SQL para insertar en la tabla Usuario y Conserje. */
    @Override
    public void agregar(Conserje conserje) throws SQLException {
        String sqlUser = "INSERT INTO Usuario (nombre, apellido, " +
                "email, dni, telefono, nombreUsuario, contrasenia, " +
                "activo, tipoUsuario) " +
                "values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlConserje = "INSERT INTO Conserje " +
                "(usuarioId, salario, turno) values(?, ?, ?)";

        try (Connection connection = ConexionDB.getConexion()) {
            // Evita que se guarde el Usuario solo si llega a fallar el guardado del Conserje
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sqlUser, PreparedStatement.RETURN_GENERATED_KEYS);
                 PreparedStatement psConserje = connection.prepareStatement(sqlConserje)) {

                ps.setString(1, conserje.getNombre());
                ps.setString(2, conserje.getApellido());
                ps.setString(3, conserje.getEmail());
                ps.setString(4, conserje.getDni());
                ps.setString(5, conserje.getTelefono());
                ps.setString(6, conserje.getNombreUsuario());
                ps.setString(7, conserje.getContrasenia());
                ps.setBoolean(8, conserje.isActivo());
                ps.setString(9, conserje.getTipoUsuario().name());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long auxId = rs.getLong(1);
                        conserje.setUsuarioId(auxId);
                        psConserje.setLong(1, auxId);
                        psConserje.setDouble(2, conserje.getSalario());
                        psConserje.setString(3, conserje.getTurno().name());
                        psConserje.executeUpdate();
                    }
                }

                // Guarda ambos registros juntos ahora que sabemos que ninguno falló
                connection.commit();

            } catch (SQLException e) {
                // Si algo salió mal en el medio, cancela todo para no dejar datos a medias
                connection.rollback();
                throw e;
            } finally {
                // Vuelve a dejar la conexión en su modo normal antes de salir
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public void modificar(Conserje conserje) throws SQLException {
        String sqlUser = "UPDATE Usuario SET nombre = ?, apellido = ?, email = ?, dni = ?, telefono = ?, " +
                "nombreUsuario = ?, contrasenia = ?, activo = ?, tipoUsuario = ? where usuarioId = ?";

        String sqlConserje = "UPDATE Conserje " +
                " SET salario = ?, turno = ? where usuarioId= ?";

        try(Connection connection = ConexionDB.getConexion()){
            connection.setAutoCommit(false);

            try( PreparedStatement ps = connection.prepareStatement(sqlUser);
                 PreparedStatement psConserje = connection.prepareStatement(sqlConserje)){

                ps.setString(1, conserje.getNombre());
                ps.setString(2, conserje.getApellido());
                ps.setString(3, conserje.getEmail());
                ps.setString(4, conserje.getDni());
                ps.setString(5, conserje.getTelefono());
                ps.setString(6, conserje.getNombreUsuario());
                ps.setString(7, conserje.getContrasenia());
                ps.setBoolean(8, conserje.isActivo());
                ps.setString(9, conserje.getTipoUsuario().name());
                ps.setLong(10, conserje.getUsuarioId());
                ps.executeUpdate();

                psConserje.setDouble(1, conserje.getSalario());
                psConserje.setString(2, conserje.getTurno().name());
                psConserje.setLong(3, conserje.getUsuarioId());
                psConserje.executeUpdate();

                connection.commit();

            }
            catch(SQLException e){
                connection.rollback();

                throw e;
            }
            finally{
                connection.setAutoCommit(true);
            }
        }
    }

    /** Usamos AND tipoUsuario = 'CONSERJE' para asegurar que el cambio se le aplica al Conserje. */
    @Override
    public void baja(long id) throws SQLException {
        String sqlUser = "UPDATE Usuario SET activo = false WHERE usuarioId = ? AND tipoUsuario = 'CONSERJE'";

        try(Connection connection = ConexionDB.getConexion();
            PreparedStatement ps = connection.prepareStatement(sqlUser)){
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Conserje buscarPorId(long id) throws SQLException {
        String sql = "SELECT u.usuarioId, u.nombre, u.apellido, u.email, u.dni, " +
                "u.telefono, u.nombreUsuario, u.contrasenia, u.activo, u.tipoUsuario, " +
                "p.salario, p.turno " +
                "FROM Usuario as u " +
                "INNER JOIN Conserje as p ON u.usuarioId = p.usuarioId " +
                "WHERE u.usuarioId = ?";

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearConserje(rs);
                }
            }
        }

        return null; // Si no encontró ningún conserje con ese ID
    }

    @Override
    public List<Conserje> listar() throws SQLException {
        String sql = "SELECT u.usuarioId, u.nombre, u.apellido, u.email, u.dni, " +
                "u.telefono, u.nombreUsuario, u.contrasenia, u.activo, u.tipoUsuario, " +
                "p.salario, p.turno " +
                "FROM Usuario as u " +
                "INNER JOIN Conserje as p ON u.usuarioId = p.usuarioId";

        List<Conserje> list = new ArrayList<>();

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapearConserje(rs));
                }

            }
        }

        return list;
    }

    @Override
    public List<Conserje> listarActivos() throws SQLException {
        String sql = "SELECT u.usuarioId, u.nombre, u.apellido, u.email, u.dni, " +
                "u.telefono, u.nombreUsuario, u.contrasenia, u.activo, u.tipoUsuario, " +
                "p.salario, p.turno " +
                "FROM Usuario as u " +
                "INNER JOIN Conserje as p ON u.usuarioId = p.usuarioId WHERE u.activo = true";

        List<Conserje> list = new ArrayList<>();

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapearConserje(rs));
                }

            }
        }

        return list;
    }

    @Override
    public List<Conserje> listarInactivos() throws SQLException {
        String sql = "SELECT u.usuarioId, u.nombre, u.apellido, u.email, u.dni, " +
                "u.telefono, u.nombreUsuario, u.contrasenia, u.activo, u.tipoUsuario, " +
                "p.salario, p.turno " +
                "FROM Usuario as u " +
                "INNER JOIN Conserje as p ON u.usuarioId = p.usuarioId WHERE u.activo = false";

        List<Conserje> list = new ArrayList<>();

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapearConserje(rs));
                }

            }
        }

        return list;
    }

    @Override
    public void reactivar(long id) throws SQLException{
        String sqlUser = "UPDATE Usuario SET activo = true WHERE usuarioId = ? AND tipoUsuario = 'CONSERJE'";

        try(Connection connection = ConexionDB.getConexion();
            PreparedStatement ps = connection.prepareStatement(sqlUser)){
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Conserje mapearConserje(ResultSet rs) throws SQLException {
        Conserje conserje = new Conserje();
        conserje.setUsuarioId(rs.getLong("usuarioId"));
        conserje.setNombre(rs.getString("nombre"));
        conserje.setApellido(rs.getString("apellido"));
        conserje.setEmail(rs.getString("email"));
        conserje.setDni(rs.getString("dni"));
        conserje.setTelefono(rs.getString("telefono"));
        conserje.setNombreUsuario(rs.getString("nombreUsuario"));
        conserje.setContrasenia(rs.getString("contrasenia"));
        conserje.setActivo(rs.getBoolean("activo"));
        conserje.setTipoUsuario(TipoUsuario.valueOf(rs.getString("tipoUsuario")));
        conserje.setSalario(rs.getDouble("salario"));
        conserje.setTurno(Turno.valueOf(rs.getString("turno")));
        return conserje;
    }
}
