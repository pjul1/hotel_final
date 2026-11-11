package com.hotel.dao;

import com.hotel.config.ConexionDB;
import com.hotel.modelo.Pasajero;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PasajeroDao implements ICrud<Pasajero>{

    @Override
    public void agregar(Pasajero pasajero) throws SQLException {
        String sqlUser = "INSERT INTO Usuario (nombre, apellido, " +
                "email, dni, telefono, nombreUsuario, contrasenia, " +
                "activo, tipoUsuario) " +
                "values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlPasajero = "INSERT INTO Pasajero " +
                "(pasajeroId, origen, domicilioOrigen) values(?, ?, ?)";

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
                ps.setObject(9, pasajero.getTipoUsuario());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int auxId = rs.getInt(1);
                        pasajero.setUsuarioId(auxId);
                        psPasajero.setInt(1, auxId);
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

    }

    @Override
    public void baja(long id) throws SQLException {

    }

    @Override
    public Pasajero buscarPorId(long id) throws SQLException {
        return null;
    }

    @Override
    public List<Pasajero> listar() throws SQLException {
        return List.of();
    }
}
