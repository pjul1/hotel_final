package com.hotel.vistas;

import com.hotel.Main;
import com.hotel.modelo.enums.TipoUsuario;
import com.hotel.modelo.enums.Turno;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AdministradorVentana {

    @FXML ComboBox<TipoUsuario> cmbTipoUsuario;
    @FXML ComboBox<Turno> cmbTurno;
    @FXML TextField txtNombre;
    @FXML TextField txtApellido;
    @FXML TextField txtDni;
    @FXML TextField txtTelefono;
    @FXML TextField txtEmail;
    @FXML TextField txtNombreUsuario;
    @FXML PasswordField txtContrasenia;
    @FXML TextField txtSalario;
    @FXML TextField txtBuscarUsuario;
    @FXML CheckBox chkActivo; ///Comprobar con un booleano chkActivo.isSelected()

    @FXML
    public void initialize(){
        cmbTipoUsuario.getItems().addAll(TipoUsuario.CONSERJE, TipoUsuario.PASAJERO);
        cmbTurno.getItems().addAll(Turno.MANIANA, Turno.TARDE, Turno.NOCHE);
    }

    @FXML
    public void salir(){
        ((Stage) txtNombre.getScene().getWindow()).close();
    }
}
