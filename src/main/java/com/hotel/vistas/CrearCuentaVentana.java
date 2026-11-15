package com.hotel.vistas;

import com.hotel.dao.PasajeroDao;
import com.hotel.modelo.Pasajero;
import com.hotel.servicios.PasajeroServicio;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.SQLException;

public class CrearCuentaVentana {
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDni;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtNombreUsuario;
    @FXML private PasswordField txtContrasenia;
    @FXML private PasswordField txtConfirmarContrasenia;
    @FXML private TextField txtOrigen;
    @FXML private TextField txtDomicilioOrigen;
    @FXML private Text txtDatosIncorrectos;
    private PasajeroDao pasajeroDao = new PasajeroDao();

    @FXML
    public void cancelar(){
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    @FXML
    public void registrarse() {
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String email = txtEmail.getText().trim();
        String dni = txtDni.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String usuario = txtNombreUsuario.getText().trim();
        String password = txtContrasenia.getText();
        String confirmPass = txtConfirmarContrasenia.getText();
        String origen = txtOrigen.getText().trim();
        String domicilio = txtDomicilioOrigen.getText().trim();
        txtDatosIncorrectos.setText("");

        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() ||
                email.isEmpty() || usuario.isEmpty() || password.isEmpty() ||
                origen.isEmpty() || domicilio.isEmpty()) {

            txtDatosIncorrectos.setText("Por favor, complete todos los campos obligatorios (*).");
            return;
        }

        if (!dni.matches("\\d{7,9}")) {
            txtDatosIncorrectos.setText("El DNI debe contener solo números (sin puntos ni guiones).");
            return;
        } ///SE USA REGEX PARA CONFIRMAR QUE EL DNI SOLO CONTENGA NUMEROS

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            txtDatosIncorrectos.setText("Ingrese un formato de email válido (ej: usuario@correo.com).");
            return;
        } ///SE USA REGEX PARA CONFIRMAR QUE EL EMAIL SOLO CONTENGA CARACTERES VALIDOS

        if (password.length() < 6) {
            txtDatosIncorrectos.setText("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        if (!password.equals(confirmPass)) {
            txtDatosIncorrectos.setText("Las contraseñas no coinciden.");
            return;
        }

        txtDatosIncorrectos.setText("");

        ///GUARDAMOS EN SERVICE Y BD
        PasajeroServicio.getInstancia().agregar(new Pasajero(nombre, apellido, email, dni, telefono, usuario, password, origen, domicilio));
        txtDatosIncorrectos.setText("Usuario creado exitosamente");
    }
}
