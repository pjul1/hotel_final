package com.hotel.vistas;

import com.hotel.Main;
import com.hotel.dao.ConserjeDao;
import com.hotel.dao.HabitacionDao;
import com.hotel.excepciones.HotelException;
import com.hotel.modelo.*;
import com.hotel.modelo.enums.*;
import com.hotel.servicios.ConserjeServicio;
import com.hotel.servicios.PasajeroServicio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
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
    @FXML private ComboBox<TipoUsuario> cmbTipoUsuario;
    @FXML private ComboBox<Turno> cmbTurno;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDni;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtNombreUsuario;
    @FXML private PasswordField txtContrasenia;
    @FXML private TextField txtSalario;
    @FXML private TextField txtBuscarUsuario;
    @FXML private CheckBox chkActivo; ///Comprobar con un booleano chkActivo.isSelected()
    @FXML private TableColumn<Usuario, Long> colUsuarioId;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colApellido;
    @FXML private TableColumn<Usuario, String> colDni;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, TipoUsuario> colRol;
    @FXML private TableColumn<Usuario, Boolean> colActivo;
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private Text txtErrorAlGuardar;
    private Usuario usuarioSeleccionado; // Sirve para guardar el usuario que se clickea
    private ObservableList<Usuario> listaUsuarios = FXCollections.observableArrayList();
    private ConserjeDao conserjeDao = new ConserjeDao(); ///SOLO PARA PRUEBAS

    /** TAB DE CONFIGURACION DE HABITACIONES */
    @FXML private TextField txtNumeroHabitacion;
    @FXML private ComboBox<TipoHabitacion> cmbTipoHabitacion;
    @FXML private ComboBox<EstadoHabitacion> cmbEstadoHabitacion;
    @FXML private ComboBox<MotivoBloqueo> cmbMotivoBloqueo;
    @FXML private TableColumn<Habitacion, Long> colHabId;
    @FXML private TableColumn<Habitacion, Integer> colNumHab;
    @FXML private TableColumn<Habitacion, TipoHabitacion> colTipoHab;
    @FXML private TableColumn<Habitacion, EstadoHabitacion> colEstadoHab;
    @FXML private TableColumn<Habitacion, MotivoBloqueo> colMotivoBloqueo;
    @FXML private TableColumn<Habitacion, Boolean> colHabActiva;
    @FXML private TableView<Habitacion> tablaHabitaciones;
    private ObservableList<Habitacion> listaHabitaciones = FXCollections.observableArrayList();
    private Habitacion habitacionSeleccionada;
    private HabitacionDao habitacionDao = new HabitacionDao();  ///SOLO PARA PRUEBAS

    @FXML
    public void initialize() throws SQLException {
        // Configuramos las columnas para que lean las properties de la clase Usuario
        colUsuarioId.setCellValueFactory(new PropertyValueFactory<>("usuarioId"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("tipoUsuario"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        // Configuramos las columnas para que lean las properties de la clase Habitacion
        colHabId.setCellValueFactory(new PropertyValueFactory<>("habitacionId"));
        colNumHab.setCellValueFactory(new PropertyValueFactory<>("numeroHabitacion"));
        colTipoHab.setCellValueFactory(new PropertyValueFactory<>("tipoHabitacion"));
        colEstadoHab.setCellValueFactory(new PropertyValueFactory<>("estadoHabitaion"));
        colMotivoBloqueo.setCellValueFactory(new PropertyValueFactory<>("motivoBloqueo"));
        colHabActiva.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // Vinculamos la lista a la tabla
        listaUsuarios.addAll(conserjeDao.listar()); ///SOLO PARA PRUEBAS DESPUES SE DEBE USAR EL SERVICE
        listaHabitaciones.addAll(habitacionDao.listar());
        tablaUsuarios.setItems(listaUsuarios);
        tablaHabitaciones.setItems(listaHabitaciones);

        cmbTipoUsuario.getItems().addAll(TipoUsuario.CONSERJE, TipoUsuario.PASAJERO);
        cmbTurno.getItems().addAll(Turno.values());
        cmbTipoHabitacion.getItems().addAll(TipoHabitacion.values());
        cmbEstadoHabitacion.getItems().addAll(EstadoHabitacion.values());
        cmbMotivoBloqueo.getItems().addAll(MotivoBloqueo.values());
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
    public void completarConClickUsuario(){
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

    @FXML
    public void completarConClickHabitacion(){
        habitacionSeleccionada = tablaHabitaciones.getSelectionModel().getSelectedItem();

        if(habitacionSeleccionada != null){
            txtNumeroHabitacion.setText(String.valueOf(habitacionSeleccionada.getNumeroHabitacion()));
            cmbTipoHabitacion.setValue(habitacionSeleccionada.getTipoHabitacion());
            cmbEstadoHabitacion.setValue(habitacionSeleccionada.getEstadoHabitacion());
            cmbMotivoBloqueo.setValue(habitacionSeleccionada.getMotivoBloqueo());
        }
    }

    /** Limpia los campos para ingresar texto, uno para cada tab */

    @FXML
    public void limpiarUsuario(){
        txtNombre.clear();
        txtApellido.clear();
        txtDni.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtNombreUsuario.clear();
        txtContrasenia.clear();
        txtSalario.clear();
    }

    @FXML
    public void limpiarHabitacion(){
        txtNumeroHabitacion.clear();
        cmbTipoHabitacion.setValue(null);
        cmbEstadoHabitacion.setValue(null);
        cmbMotivoBloqueo.setValue(null);
    }

    /** Permite buscar en la tabla en tiempo real */

    @FXML
    public void buscar(){
        ///NECESITO LOS SERVICE
    }

    /** Guarda en base de datos y memoria a traves del service */

    @FXML
    public void guardar() {
        TipoUsuario tipoUsuario = cmbTipoUsuario.getValue();
        if (tipoUsuario == null) {
            txtErrorAlGuardar.setText("Seleccione un tipo de usuario.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String dni = txtDni.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String email = txtEmail.getText().trim();
        String nombreUsuario = txtNombreUsuario.getText().trim();
        String contrasenia = txtContrasenia.getText().trim();

        try {
            if (tipoUsuario == TipoUsuario.CONSERJE) {
                Turno turno = cmbTurno.getValue();
                if (turno == null || txtSalario.getText().trim().isEmpty()) {
                    txtErrorAlGuardar.setText("Complete el turno y el salario del conserje.");
                    return;
                }

                double salario;
                try {
                    salario = Double.parseDouble(txtSalario.getText().trim());
                } catch (NumberFormatException e) {
                    txtErrorAlGuardar.setText("El salario debe ser un número válido (ej: 450000.50).");
                    return;
                }

                Conserje conserjeGuardar = new Conserje(nombre, apellido, email, dni, telefono, nombreUsuario, contrasenia, salario, turno);
                conserjeGuardar.setActivo(chkActivo.isSelected());

                ConserjeServicio.getInstancia().agregar(conserjeGuardar);

            } else if (tipoUsuario == TipoUsuario.PASAJERO) {
                Pasajero pasajeroGuardar = new Pasajero(nombre, apellido, email, dni, telefono, nombreUsuario, contrasenia, "", "");
                pasajeroGuardar.setActivo(chkActivo.isSelected());

                PasajeroServicio.getInstancia().agregar(pasajeroGuardar);
            }

            //cargarTablaUsuarios();
            limpiarUsuario();
            txtErrorAlGuardar.setText("Usuario registrado correctamente.");

        } catch (HotelException e) {
            txtErrorAlGuardar.setText(e.getMessage());
        }
    }











}
