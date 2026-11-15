package com.hotel.vistas;

import com.hotel.modelo.Administrador;
import com.hotel.modelo.Conserje;
import com.hotel.modelo.Pasajero;
import com.hotel.modelo.Usuario;
import com.hotel.servicios.AdministradorServicio;
import com.hotel.servicios.ConserjeServicio;
import com.hotel.servicios.PasajeroServicio;
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
import com.hotel.config.*;

public class LoginVentana {
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPassword;
    @FXML private Text datosIncorrectos;

    @FXML
    void iniciarSesion(ActionEvent event) {
        String user = txtUser.getText().trim();
        String password = txtPassword.getText().trim();

        if (user.isEmpty() || password.isEmpty()) {
            datosIncorrectos.setText("Complete usuario y contraseña.");
            return;
        }

        Usuario usuarioLogueado = null;

        Administrador admin = AdministradorServicio.getInstancia().obtenerAdministrador();
        if (admin != null && admin.getNombreUsuario().equals(user) && admin.getContrasenia().equals(password)) {
            usuarioLogueado = admin;
        }

        if (usuarioLogueado == null) {
            for (Conserje c : ConserjeServicio.getInstancia().listar()) {
                if (c.getNombreUsuario().equals(user) && c.getContrasenia().equals(password)) {
                    usuarioLogueado = c;
                    break;
                }
            }
        }

        if (usuarioLogueado == null) {
            for (Pasajero p : PasajeroServicio.getInstancia().listar()) {
                if (p.getNombreUsuario().equals(user) && p.getContrasenia().equals(password)) {
                    usuarioLogueado = p;
                    break;
                }
            }
        }

        if (usuarioLogueado == null) {
            datosIncorrectos.setText("Usuario o contraseña incorrectos");
            return;
        }

        if (!usuarioLogueado.isActivo()) {
            datosIncorrectos.setText("Su cuenta se encuentra desactivada. Contacte al administrador.");
            return;
        }

        try {
            String fxmlFile = "";
            String windowTitle = "Sistema de administración";

            if (usuarioLogueado instanceof Administrador) {
                fxmlFile = "/views/dashboard/main-screen-admin.fxml";
            } else if (usuarioLogueado instanceof Conserje) {
                fxmlFile = "/views/dashboard/main-screen-conserje.fxml";
                windowTitle = "Recepción y Conserjería";
            } else if (usuarioLogueado instanceof Pasajero) {
                fxmlFile = "/views/dashboard/main-screen-pasajero.fxml";
                windowTitle = "Sistema de reserva y pedidos";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            if (usuarioLogueado instanceof Pasajero) {
                PasajeroVentana controladorPasajero = loader.getController();
                controladorPasajero.setPasajeroLogueado((Pasajero) usuarioLogueado);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle(windowTitle);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            datosIncorrectos.setText("Error al cargar la pantalla del sistema.");
        }
    }


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
