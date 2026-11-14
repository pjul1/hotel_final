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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Locale;

public class AdministradorVentana {

    /** TAB DE GESTION DE USUARIOS Y ROLES */
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

    /** TAB DE CONFIGURACION DE HABITACIONES */



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
    public void generarBackup() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Copia de Seguridad");
        fileChooser.setInitialFileName("backup_hotel_" + java.time.LocalDate.now() + ".sql");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo SQL (*.sql)", "*.sql"));

        Stage stage = (Stage) txtNombre.getScene().getWindow();  /** OBTENEMOS LA STAGE ACTUAL DE LA VENTANA */
        File archivoDestino = fileChooser.showSaveDialog(stage); /** SIRVE PARA BLOQUEAR LA PANTALLA HASTA QUE TERMINE EL GUARDADO */

        if (archivoDestino == null) {
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(archivoDestino))) {
            /** ESCRIBIMOS DENTRO DEL ARCHIVO SQL DE BACKUP */
            writer.println("-- BACKUP DEL SISTEMA HOTELERO");
            writer.println("-- Fecha: " + java.time.LocalDateTime.now());
            writer.println();

            writer.println("-- TABLA USUARIO");
            for (Usuario u : listaUsuarios) { //TENEMOS QUE CAMBIAR listaUsuarios POR EL SERVICE!!!!!!!
                writer.println("INSERT INTO Usuario (usuarioId, nombre, apellido, email, dni, telefono, nombreUsuario, contrasenia, activo, tipoUsuario) VALUES (" +
                        u.getUsuarioId() + ", '" + u.getNombre() + "', '" + u.getApellido() + "', '" +
                        u.getEmail() + "', '" + u.getDni() + "', '" + u.getTelefono() + "', '" +
                        u.getNombreUsuario() + "', '" + u.getContrasenia() + "', " + u.isActivo() + ", '" +
                        u.getTipoUsuario() + "');");
            }

            writer.println();

            System.out.println("Se realizo el backup");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al realizar el backup");
        }
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

    /** Limpia los campos para ingresar texto */

    @FXML
    public void limpiar(){
        txtNombre.clear();
        txtApellido.clear();
        txtDni.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtNombreUsuario.clear();
        txtContrasenia.clear();
        txtSalario.clear();
    }

    /** Permite buscar en la tabla en tiempo real */

    @FXML
    public void buscar(){
        ///NECESITO LOS SERVICE
    }



}
