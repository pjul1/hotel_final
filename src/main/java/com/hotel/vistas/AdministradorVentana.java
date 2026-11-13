package com.hotel.vistas;

import com.hotel.Main;
import com.hotel.dao.ConserjeDao;
import com.hotel.modelo.Administrador;
import com.hotel.modelo.Conserje;
import com.hotel.modelo.Usuario;
import com.hotel.modelo.enums.TipoUsuario;
import com.hotel.modelo.enums.Turno;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;

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
    @FXML TableColumn<Usuario, Long> colUsuarioId;
    @FXML TableColumn<Usuario, String> colNombre;
    @FXML TableColumn<Usuario, String> colApellido;
    @FXML TableColumn<Usuario, String> colDni;
    @FXML TableColumn<Usuario, String> colEmail;
    @FXML TableColumn<Usuario, TipoUsuario> colRol;
    @FXML TableColumn<Usuario, Boolean> colActivo;
    @FXML TableView<Usuario> tablaUsuarios;
    private Usuario usuarioSeleccionado; // Sirve para guardar el usuario que se clickea
    private ObservableList<Usuario> listaUsuarios = FXCollections.observableArrayList();
    private ConserjeDao conserjeDao = new ConserjeDao();

    @FXML
    public void initialize() throws SQLException {
        // Configuramos las columnas para que lean las properties de la clase Producto
        colUsuarioId.setCellValueFactory(new PropertyValueFactory<>("usuarioId"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("tipoUsuario"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // Vinculamos la lista a la tabla
        listaUsuarios.addAll(conserjeDao.listar()); ///SOLO PARA PRUEBAS DESPUES SE DEBE USAR EL SERVICE
        tablaUsuarios.setItems(listaUsuarios);

        cmbTipoUsuario.getItems().addAll(TipoUsuario.CONSERJE, TipoUsuario.PASAJERO);
        cmbTurno.getItems().addAll(Turno.MANIANA, Turno.TARDE, Turno.NOCHE);
    }

    @FXML
    public void salir(){
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    @FXML
    public void completarConClick(){
        usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if(usuarioSeleccionado != null){
            txtNombre.setText(usuarioSeleccionado.getNombre());
            txtApellido.setText(usuarioSeleccionado.getApellido());
            txtDni.setText(usuarioSeleccionado.getDni());
            txtTelefono.setText(usuarioSeleccionado.getTelefono());
            txtEmail.setText(usuarioSeleccionado.getEmail());
            txtNombreUsuario.setText(usuarioSeleccionado.getNombreUsuario());
            txtContrasenia.setText(usuarioSeleccionado.getContrasenia());
            chkActivo.setSelected(usuarioSeleccionado.isActivo());
            cmbTipoUsuario.setValue(usuarioSeleccionado.getTipoUsuario());

            if (usuarioSeleccionado instanceof Conserje) {
                Conserje conserje = (Conserje) usuarioSeleccionado;
                cmbTipoUsuario.setDisable(false);
                cmbTurno.setDisable(false);
                txtSalario.setDisable(false);
                cmbTurno.setValue(conserje.getTurno());
                txtSalario.setText(String.valueOf(conserje.getSalario()));
            } else {
                // Bloquear campos que solo pertenecen a conserje si no es un conserje
                cmbTurno.setValue(null);
                txtSalario.clear();
                cmbTurno.setDisable(true);
                txtSalario.setDisable(true);
                if(usuarioSeleccionado.getTipoUsuario().equals(TipoUsuario.ADMINISTRADOR)){
                    cmbTipoUsuario.setDisable(true);
                }
            }
        }
    }
}
