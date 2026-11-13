package com.hotel.dao;

import com.hotel.config.ConexionDB;
import com.hotel.modelo.Administrador;
import com.hotel.modelo.enums.TipoUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Maneja la persistencia del administrador único del sistema.
 * AdministradorDao no implementa ninguna interfaz, ya que:
 * -al solo existir un Administrador no tiene sentido listar,
 * -tiene baja lógica, pero no la utiliza (Un Administrador único no puede quedar inactivo),
 * -No se puede agregar un nuevo Administrador.
 * */
public class AdministradorDao {

    /** Actualiza los datos y credenciales del administrador. */
    public void modificar(Administrador administrador) throws SQLException {
        /* No se pueden modificar los datos 'activo' y 'tipoUsuario'
           usamos AND tipoUsuario = 'ADMINISTRADOR' para asegurar que el cambio se le aplica al Admin.
         */
        String sql = "UPDATE Usuario SET nombre = ?, apellido = ?, email = ?, dni = ?, telefono = ?, " +
                "nombreUsuario = ?, contrasenia = ? where usuarioId = ? AND tipoUsuario = 'ADMINISTRADOR'";

        try(Connection connection = ConexionDB.getConexion();
            PreparedStatement ps = connection.prepareStatement(sql)){

                ps.setString(1, administrador.getNombre());
                ps.setString(2, administrador.getApellido());
                ps.setString(3, administrador.getEmail());
                ps.setString(4, administrador.getDni());
                ps.setString(5, administrador.getTelefono());
                ps.setString(6, administrador.getNombreUsuario());
                ps.setString(7, administrador.getContrasenia());
                ps.setLong(8, administrador.getUsuarioId());
                ps.executeUpdate();
            }
    }

    /** Trae al administrador desde la base de datos (o null si no está cargado). */
    public Administrador obtenerAdministrador() throws SQLException {
        String sql = "SELECT * FROM Usuario WHERE tipoUsuario = 'ADMINISTRADOR' limit 1";

        try(Connection connection = ConexionDB.getConexion();
            PreparedStatement ps = connection.prepareStatement(sql)){

            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearAdministrador(rs);
                }
            }
        }
        return null;
    }
    /** Arma un objeto Administrador a partir de una fila del ResultSet. */
    private Administrador mapearAdministrador(ResultSet rs) throws SQLException {
        Administrador administrador = new Administrador();
        administrador.setUsuarioId(rs.getLong("usuarioId"));
        administrador.setNombre(rs.getString("nombre"));
        administrador.setApellido(rs.getString("apellido"));
        administrador.setEmail(rs.getString("email"));
        administrador.setDni(rs.getString("dni"));
        administrador.setTelefono(rs.getString("telefono"));
        administrador.setNombreUsuario(rs.getString("nombreUsuario"));
        administrador.setContrasenia(rs.getString("contrasenia"));
        administrador.setActivo(rs.getBoolean("activo"));
        administrador.setTipoUsuario(TipoUsuario.valueOf(rs.getString("tipoUsuario")));
        return administrador;
    }

}
