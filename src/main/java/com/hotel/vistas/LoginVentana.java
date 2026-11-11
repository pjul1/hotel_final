package com.hotel.vistas;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginVentana {
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPassword;
    @FXML private Text datosIncorrectos;
/*
    @FXML
    void iniciarSesion(ActionEvent event){
        String user = txtUser.getText();
        String password = txtPassword.getText();

        String sql = "SELECT nombreUsuario, contrasenia, tipoUsuario FROM usuario WHERE nombreUsuario = ? AND contrasenia = ?";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    String rol = rs.getString("tipoUsuario");
                    String fxmlFile = "";
                    String windowTitle = "Sistema de administración";

                    switch (rol.toLowerCase()) {
                        case "administrador":
                            fxmlFile = "/views/dashboard/main-screen-admin.fxml";
                            break;
                        case "conserje":
                            fxmlFile = "/views/dashboard/main-screen-conserje.fxml";
                            break;
                        case "pasajero":
                            fxmlFile = "/views/dashboard/main-screen-pasajero.fxml";
                            windowTitle = "Sistema de reserva y pedidos";
                            break;
                        default:
                            datosIncorrectos.setText("Rol no asignado al sistema.");
                            return;
                    }

                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                    Parent root = loader.load();

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    Scene scene = new Scene(root);

                    stage.setScene(scene);
                    stage.setTitle(windowTitle);
                    stage.centerOnScreen();
                    stage.show();

                } else {
                    datosIncorrectos.setText("Usuario o contraseña incorrectos");
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            datosIncorrectos.setText("Error de conexión con el sistema.");
        }
    }

 */

    @FXML
    void nuevoUsuario(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login/crearCuenta.fxml"));
            Parent root = loader.load();
            Stage userWindow = new Stage();
            userWindow.setTitle("Crear cuenta");
            userWindow.setScene(new Scene(root));
            userWindow.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
