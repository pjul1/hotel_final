package com.hotel.vistas;

import com.hotel.Main;
import com.hotel.dao.ConserjeDao;
import com.hotel.dao.HabitacionDao;
import com.hotel.excepciones.HotelException;
import com.hotel.modelo.*;
import com.hotel.modelo.enums.*;
import com.hotel.servicios.*;
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
import java.util.ArrayList;
import java.util.List;
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
    @FXML private Text txtMensajeHabitacion;
    private Usuario usuarioSeleccionado; // Sirve para guardar el usuario que se clickea
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
    @FXML private CheckBox chkHabitacionActiva;
    private ObservableList<Habitacion> listaHabitaciones = FXCollections.observableArrayList();
    private Habitacion habitacionSeleccionada;
    private HabitacionDao habitacionDao = new HabitacionDao();  ///SOLO PARA PRUEBAS

    @FXML
    public void initialize(){
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
        colEstadoHab.setCellValueFactory(new PropertyValueFactory<>("estadoHabitacion"));
        colMotivoBloqueo.setCellValueFactory(new PropertyValueFactory<>("motivoBloqueo"));
        colHabActiva.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // Vinculamos la lista a la tabla
        cargarTablaUsuarios();
        cargarTablaHabitaciones();

        cmbTipoUsuario.getItems().addAll(TipoUsuario.CONSERJE, TipoUsuario.PASAJERO);
        cmbTurno.getItems().addAll(Turno.values());
        cmbTipoHabitacion.getItems().addAll(TipoHabitacion.values());
        cmbEstadoHabitacion.getItems().addAll(EstadoHabitacion.values());
        cmbMotivoBloqueo.getItems().addAll(MotivoBloqueo.values());
    }

    private void cargarTablaUsuarios() {
        List<Usuario> todosLosUsuarios = new ArrayList<>();

        Administrador admin = AdministradorServicio.getInstancia().obtenerAdministrador();
        if (admin != null) {
            todosLosUsuarios.add(admin);
        }

        todosLosUsuarios.addAll(ConserjeServicio.getInstancia().listar());
        todosLosUsuarios.addAll(PasajeroServicio.getInstancia().listar());

        tablaUsuarios.setItems(FXCollections.observableArrayList(todosLosUsuarios));
    }

    private void cargarTablaHabitaciones() {
        tablaHabitaciones.setItems(FXCollections.observableArrayList(
                HabitacionServicio.getInstancia().listar()
        ));
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

        Stage stage = (Stage) txtNombre.getScene().getWindow();
        File archivoDestino = fileChooser.showSaveDialog(stage);

        if (archivoDestino == null) {
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(archivoDestino))) {
            writer.println("-- BACKUP DEL SISTEMA HOTELERO");
            writer.println("-- Fecha: " + java.time.LocalDateTime.now());
            writer.println();

            // 1 Exportar Administrador único
            writer.println("-- TABLA USUARIO (ADMINISTRADOR)");
            Administrador admin = AdministradorServicio.getInstancia().obtenerAdministrador();
            if (admin != null) {
                writer.println("INSERT INTO Usuario (usuarioId, nombre, apellido, email, dni, telefono, nombreUsuario, contrasenia, activo, tipoUsuario) VALUES (" +
                        admin.getUsuarioId() + ", '" + admin.getNombre() + "', '" + admin.getApellido() + "', '" +
                        admin.getEmail() + "', '" + admin.getDni() + "', '" + admin.getTelefono() + "', '" +
                        admin.getNombreUsuario() + "', '" + admin.getContrasenia() + "', " + admin.isActivo() + ", '" +
                        admin.getTipoUsuario() + "');");
                writer.println("INSERT INTO Administrador (usuarioId) VALUES (" + admin.getUsuarioId() + ");");
            }

            // 2 Exportar Conserjes (Tabla Usuario + Tabla Conserje)
            writer.println("\n-- TABLA USUARIO Y CONSERJE");
            for (Conserje c : ConserjeServicio.getInstancia().listar()) {
                writer.println("INSERT INTO Usuario (usuarioId, nombre, apellido, email, dni, telefono, nombreUsuario, contrasenia, activo, tipoUsuario) VALUES (" +
                        c.getUsuarioId() + ", '" + c.getNombre() + "', '" + c.getApellido() + "', '" +
                        c.getEmail() + "', '" + c.getDni() + "', '" + c.getTelefono() + "', '" +
                        c.getNombreUsuario() + "', '" + c.getContrasenia() + "', " + c.isActivo() + ", '" +
                        c.getTipoUsuario() + "');");
                writer.println("INSERT INTO Conserje (usuarioId, turno, salario) VALUES (" +
                        c.getUsuarioId() + ", '" + c.getTurno() + "', " + c.getSalario() + ");");
            }

            // 3 Exportar Pasajeros (Tabla Usuario + Tabla Pasajero)
            writer.println("\n-- TABLA USUARIO Y PASAJERO");
            for (Pasajero p : PasajeroServicio.getInstancia().listar()) {
                writer.println("INSERT INTO Usuario (usuarioId, nombre, apellido, email, dni, telefono, nombreUsuario, contrasenia, activo, tipoUsuario) VALUES (" +
                        p.getUsuarioId() + ", '" + p.getNombre() + "', '" + p.getApellido() + "', '" +
                        p.getEmail() + "', '" + p.getDni() + "', '" + p.getTelefono() + "', '" +
                        p.getNombreUsuario() + "', '" + p.getContrasenia() + "', " + p.isActivo() + ", '" +
                        p.getTipoUsuario() + "');");
                writer.println("INSERT INTO Pasajero (usuarioId, origen, domicilioOrigen) VALUES (" +
                        p.getUsuarioId() + ", '" + p.getOrigen() + "', '" + p.getDomicilioOrigen() + "');");
            }

            // 4 Exportar Habitaciones
            writer.println("\n-- TABLA HABITACION");
            for (Habitacion h : HabitacionServicio.getInstancia().listar()) {
                writer.println("INSERT INTO Habitacion (habitacionId, numeroHabitacion, tipoHabitacion, estadoHabitacion, motivoBloqueo, activa) VALUES (" +
                        h.getHabitacionId() + ", " + h.getNumeroHabitacion() + ", '" + h.getTipoHabitacion() + "', '" +
                        h.getEstadoHabitacion() + "', '" + h.getMotivoBloqueo() + "', " + h.isActivo() + ");");
            }

            // 5 Exportar Reservas
            writer.println("\n-- TABLA RESERVA");
            for (Reserva r : ReservaServicio.getInstancia().listar()) {
                writer.println("INSERT INTO Reserva (reservaId, precio, cantidadPasajeros, estadoReserva, duracionEstadia, diaReservado, checkIn, checkOut, habitacion, pasajero) VALUES (" +
                        r.getReservaId() + ", " + r.getPrecio() + ", " + r.getCantidadPasajeros() + ", '" +
                        r.getEstadoReserva() + "', " + r.getDuracionEstadia() + ", '" + r.getDiaReservado() + "', " +
                        (r.getCheckIn() != null ? "'" + r.getCheckIn() + "'" : "NULL") + ", " +
                        (r.getCheckOut() != null ? "'" + r.getCheckOut() + "'" : "NULL") + ", " +
                        r.getHabitacion().getHabitacionId() + ", " + r.getPasajero().getUsuarioId() + ");");
            }

            System.out.println("Se realizó el backup completo.");

        } catch (IOException e) {
            e.printStackTrace();
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
                cmbTipoUsuario.setDisable(false);
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
            chkHabitacionActiva.setSelected(habitacionSeleccionada.isActivo());
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
        chkHabitacionActiva.setSelected(true);
    }

    /** Permite buscar en la tabla en tiempo real */

    @FXML
    public void buscarUsuario() {
        String textoBusqueda = txtBuscarUsuario.getText().trim().toLowerCase();

        if (textoBusqueda.isEmpty()) {
            cargarTablaUsuarios();
            return;
        }

        List<Usuario> todos = new ArrayList<>();
        Administrador admin = AdministradorServicio.getInstancia().obtenerAdministrador();
        if (admin != null) {
            todos.add(admin);
        }
        todos.addAll(ConserjeServicio.getInstancia().listar());
        todos.addAll(PasajeroServicio.getInstancia().listar());

        List<Usuario> filtrados = new ArrayList<>();
        for (Usuario u : todos) {
            boolean coincideNombre = u.getNombre().toLowerCase().contains(textoBusqueda);
            boolean coincideApellido = u.getApellido().toLowerCase().contains(textoBusqueda);
            boolean coincideDni = u.getDni().toLowerCase().contains(textoBusqueda);
            boolean coincideRol = u.getTipoUsuario().toString().toLowerCase().contains(textoBusqueda);

            if (coincideNombre || coincideApellido || coincideDni || coincideRol) {
                filtrados.add(u);
            }
        }

        tablaUsuarios.setItems(FXCollections.observableArrayList(filtrados));
    }


    /** Guarda en base de datos y memoria a traves del service */

    @FXML
    public void guardarUsuario() {
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

            cargarTablaUsuarios();
            limpiarUsuario();
            txtErrorAlGuardar.setText("Usuario registrado correctamente.");

        } catch (HotelException e) {
            txtErrorAlGuardar.setText(e.getMessage());
        }
    }

    @FXML
    public void guardarHabitacion() {
        txtMensajeHabitacion.setText("");

        if (habitacionSeleccionada != null) {
            txtMensajeHabitacion.setText("Tiene una habitación seleccionada. Presione 'Limpiar' para crear una nueva.");
            return;
        }

        String textoNumero = txtNumeroHabitacion.getText().trim();
        if (textoNumero.isEmpty()) {
            txtMensajeHabitacion.setText("Ingrese el número de habitación.");
            return;
        }

        int numeroHabitacion;
        try {
            numeroHabitacion = Integer.parseInt(textoNumero);
        } catch (NumberFormatException e) {
            txtMensajeHabitacion.setText("El número de habitación debe ser un valor numérico.");
            return;
        }

        TipoHabitacion tipo = cmbTipoHabitacion.getValue();
        EstadoHabitacion estado = cmbEstadoHabitacion.getValue();
        MotivoBloqueo motivo = cmbMotivoBloqueo.getValue();

        if (tipo == null || estado == null) {
            txtMensajeHabitacion.setText("Seleccione el tipo y el estado de la habitación.");
            return;
        }

        try {
            Habitacion nuevaHabitacion = new Habitacion(numeroHabitacion, tipo);
            nuevaHabitacion.setEstadoHabitacion(estado);
            nuevaHabitacion.setMotivoBloqueo(motivo);

            HabitacionServicio.getInstancia().agregar(nuevaHabitacion);

            cargarTablaHabitaciones();
            limpiarHabitacion();
            txtMensajeHabitacion.setText("Habitación registrada correctamente.");

        } catch (HotelException e) {
            txtMensajeHabitacion.setText(e.getMessage());
        }
    }

    @FXML
    public void modificarUsuario() {
        txtErrorAlGuardar.setText("");

        if (usuarioSeleccionado == null) {
            txtErrorAlGuardar.setText("Seleccione un usuario de la tabla para modificar.");
            return;
        }

        usuarioSeleccionado.setNombre(txtNombre.getText().trim());
        usuarioSeleccionado.setApellido(txtApellido.getText().trim());
        usuarioSeleccionado.setDni(txtDni.getText().trim());
        usuarioSeleccionado.setTelefono(txtTelefono.getText().trim());
        usuarioSeleccionado.setEmail(txtEmail.getText().trim());
        usuarioSeleccionado.setNombreUsuario(txtNombreUsuario.getText().trim());
        usuarioSeleccionado.setContrasenia(txtContrasenia.getText().trim());

        usuarioSeleccionado.setActivo(chkActivo.isSelected());

        try {
            if (usuarioSeleccionado instanceof Administrador) {
                AdministradorServicio.getInstancia().modificar((Administrador) usuarioSeleccionado);

            } else if (usuarioSeleccionado instanceof Conserje) {
                Conserje conserje = (Conserje) usuarioSeleccionado;
                Turno turno = cmbTurno.getValue();
                if (turno == null || txtSalario.getText().trim().isEmpty()) {
                    txtErrorAlGuardar.setText("Complete el turno y el salario del conserje.");
                    return;
                }
                try {
                    conserje.setSalario(Double.parseDouble(txtSalario.getText().trim()));
                } catch (NumberFormatException e) {
                    txtErrorAlGuardar.setText("El salario debe ser un número válido.");
                    return;
                }
                conserje.setTurno(turno);
                ConserjeServicio.getInstancia().modificar(conserje);

            } else if (usuarioSeleccionado instanceof Pasajero) {
                PasajeroServicio.getInstancia().modificar((Pasajero) usuarioSeleccionado);
            }

            tablaUsuarios.refresh();
            limpiarUsuario();
            txtErrorAlGuardar.setText("Usuario modificado correctamente.");

        } catch (HotelException e) {
            txtErrorAlGuardar.setText(e.getMessage());
        }
    }

    @FXML
    public void actualizarHabitacion() {
        txtMensajeHabitacion.setText("");

        if (habitacionSeleccionada == null) {
            txtMensajeHabitacion.setText("Seleccione una habitación de la tabla para actualizar.");
            return;
        }

        String textoNumero = txtNumeroHabitacion.getText().trim();
        if (textoNumero.isEmpty()) {
            txtMensajeHabitacion.setText("Ingrese el número de habitación.");
            return;
        }

        int numeroHabitacion;
        try {
            numeroHabitacion = Integer.parseInt(textoNumero);
        } catch (NumberFormatException e) {
            txtMensajeHabitacion.setText("El número de habitación debe ser un valor numérico.");
            return;
        }

        TipoHabitacion tipo = cmbTipoHabitacion.getValue();
        EstadoHabitacion estado = cmbEstadoHabitacion.getValue();
        MotivoBloqueo motivo = cmbMotivoBloqueo.getValue();

        if (tipo == null || estado == null) {
            txtMensajeHabitacion.setText("Seleccione el tipo y el estado de la habitación.");
            return;
        }

        try {
            habitacionSeleccionada.setNumeroHabitacion(numeroHabitacion);
            habitacionSeleccionada.setTipoHabitacion(tipo);
            habitacionSeleccionada.setEstadoHabitacion(estado);
            habitacionSeleccionada.setMotivoBloqueo(motivo);
            habitacionSeleccionada.setActivo(chkHabitacionActiva.isSelected());

            HabitacionServicio.getInstancia().modificar(habitacionSeleccionada);

            tablaHabitaciones.refresh();
            limpiarHabitacion();
            txtMensajeHabitacion.setText("Habitación actualizada correctamente.");

        } catch (HotelException e) {
            txtMensajeHabitacion.setText(e.getMessage());
        }
    }

}
