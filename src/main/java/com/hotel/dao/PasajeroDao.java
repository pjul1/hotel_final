package com.hotel.dao;

import com.hotel.config.ConexionDB;
import com.hotel.modelo.Pasajero;
import com.hotel.modelo.enums.TipoUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PasajeroDao implements ICrud<Pasajero>, IBajaLogica<Pasajero> {

    @Override
    public void agregar(Pasajero pasajero) throws SQLException {
        String sqlUser = "INSERT INTO Usuario (nombre, apellido, " +
                "email, dni, telefono, nombreUsuario, contrasenia, " +
                "activo, tipoUsuario) " +
                "values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlPasajero = "INSERT INTO Pasajero " +
                "(usuarioId, origen, domicilioOrigen) values(?, ?, ?)";

        try (Connection connection = ConexionDB.getConexion()) {
            // Evita que se guarde el Usuario solo si llega a fallar el guardado del Pasajero
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sqlUser, PreparedStatement.RETURN_GENERATED_KEYS);
                 PreparedStatement psPasajero = connection.prepareStatement(sqlPasajero)) {

                ps.setString(1, pasajero.getNombre());
                ps.setString(2, pasajero.getApellido());
                ps.setString(3, pasajero.getEmail());
                ps.setString(4, pasajero.getDni());
                ps.setString(5, pasajero.getTelefono());
                ps.setString(6, pasajero.getNombreUsuario());
                ps.setString(7, pasajero.getContrasenia());
                ps.setBoolean(8, pasajero.isActivo());
                ps.setString(9, pasajero.getTipoUsuario().name());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long auxId = rs.getLong(1);
                        pasajero.setUsuarioId(auxId);
                        psPasajero.setLong(1, auxId);
                        psPasajero.setString(2, pasajero.getOrigen());
                        psPasajero.setString(3, pasajero.getDomicilioOrigen());
                        psPasajero.executeUpdate();
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
    public void modificar(Pasajero pasajero) throws SQLException {
        String sqlUser = "UPDATE Usuario SET nombre = ?, apellido = ?, email = ?, dni = ?, telefono = ?, " +
                "nombreUsuario = ?, contrasenia = ?, activo = ?, tipoUsuario = ? where usuarioId = ?";

        String sqlPasajero = "UPDATE Pasajero " +
                " SET origen = ?, domicilioOrigen = ? where usuarioId= ?";

        try(Connection connection = ConexionDB.getConexion()){
            connection.setAutoCommit(false);

            try( PreparedStatement ps = connection.prepareStatement(sqlUser);
                 PreparedStatement psPasajero = connection.prepareStatement(sqlPasajero)){

                ps.setString(1, pasajero.getNombre());
                ps.setString(2, pasajero.getApellido());
                ps.setString(3, pasajero.getEmail());
                ps.setString(4, pasajero.getDni());
                ps.setString(5, pasajero.getTelefono());
                ps.setString(6, pasajero.getNombreUsuario());
                ps.setString(7, pasajero.getContrasenia());
                ps.setBoolean(8, pasajero.isActivo());
                ps.setString(9, pasajero.getTipoUsuario().name());
                ps.setLong(10, pasajero.getUsuarioId());
                ps.executeUpdate();

                psPasajero.setString(1, pasajero.getOrigen());
                psPasajero.setString(2, pasajero.getDomicilioOrigen());
                psPasajero.setLong(3, pasajero.getUsuarioId());
                psPasajero.executeUpdate();

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

    @Override
    public void baja(long id) throws SQLException {
        String sqlUser = "UPDATE Usuario SET activo = false WHERE usuarioId = ? AND tipoUsuario = 'PASAJERO'";

        try(Connection connection = ConexionDB.getConexion();
            PreparedStatement ps = connection.prepareStatement(sqlUser)){
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Pasajero buscarPorId(long id) throws SQLException {
        String sql = "SELECT u.usuarioId, u.nombre, u.apellido, u.email, u.dni, " +
                "u.telefono, u.nombreUsuario, u.contrasenia, u.activo, u.tipoUsuario, " +
                "p.origen, p.domicilioOrigen " +
                "FROM Usuario as u " +
                "INNER JOIN Pasajero as p ON u.usuarioId = p.usuarioId " +
                "WHERE u.usuarioId = ?";

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPasajero(rs);
                }
            }
        }

        return null; // Si no encontró ningún pasajero con ese ID
    }

    @Override
    public List<Pasajero> listar() throws SQLException {
        String sql = "SELECT u.usuarioId, u.nombre, u.apellido, u.email, u.dni, " +
                "u.telefono, u.nombreUsuario, u.contrasenia, u.activo, u.tipoUsuario, " +
                "p.origen, p.domicilioOrigen " +
                "FROM Usuario as u " +
                "INNER JOIN Pasajero as p ON u.usuarioId = p.usuarioId";

        List<Pasajero> list = new ArrayList<>();

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapearPasajero(rs));
                }

            }
        }

        return list;
    }

    @Override
    public List<Pasajero> listarActivos() throws SQLException {
        String sql = "SELECT u.usuarioId, u.nombre, u.apellido, u.email, u.dni, " +
                "u.telefono, u.nombreUsuario, u.contrasenia, u.activo, u.tipoUsuario, " +
                "p.origen, p.domicilioOrigen " +
                "FROM Usuario as u " +
                "INNER JOIN Pasajero as p ON u.usuarioId = p.usuarioId WHERE u.activo = true";

        List<Pasajero> list = new ArrayList<>();

        try (Connection connection = ConexionDB.getConexion();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapearPasajero(rs));
                }

            }
        }

        return list;
    }

    @Override
    public void reactivar(long id) throws SQLException{
        String sqlUser = "UPDATE Usuario SET activo = true WHERE usuarioId = ? AND tipoUsuario = 'PASAJERO'";

        try(Connection connection = ConexionDB.getConexion();
            PreparedStatement ps = connection.prepareStatement(sqlUser)){
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }


    private Pasajero mapearPasajero(ResultSet rs) throws SQLException{
        Pasajero pasajero = new Pasajero();
        pasajero.setUsuarioId(rs.getLong("usuarioId"));
        pasajero.setNombre(rs.getString("nombre"));
        pasajero.setApellido(rs.getString("apellido"));
        pasajero.setEmail(rs.getString("email"));
        pasajero.setDni(rs.getString("dni"));
        pasajero.setTelefono(rs.getString("telefono"));
        pasajero.setNombreUsuario(rs.getString("nombreUsuario"));
        pasajero.setContrasenia(rs.getString("contrasenia"));
        pasajero.setActivo(rs.getBoolean("activo"));
        pasajero.setTipoUsuario(TipoUsuario.valueOf(rs.getString("tipoUsuario")));
        pasajero.setOrigen(rs.getString("origen"));
        pasajero.setDomicilioOrigen(rs.getString("domicilioOrigen"));
        return pasajero;
    }
}
