package com.hotel.vistas;

import com.hotel.excepciones.HotelException;
import com.hotel.modelo.*;
import com.hotel.modelo.enums.EstadoHabitacion;
import com.hotel.modelo.enums.EstadoReserva;
import com.hotel.modelo.enums.MotivoBloqueo;
import com.hotel.modelo.enums.TipoHabitacion;
import com.hotel.servicios.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConserjeVentana {

    @FXML private Label lblNombreConserje;

    /** TAB DE RESERVA Y CHECK-IN */
    @FXML private TextField txtDniPasajeroReserva;
    @FXML private Label lblPasajeroSeleccionado;
    @FXML private DatePicker dpFechaIngreso;
    @FXML private TextField txtDuracionEstadia;
    @FXML private TextField txtCantidadPasajeros;
    @FXML private ComboBox<Integer> cmbHabitacionReserva;
    @FXML private TextField txtPrecioReserva;
    @FXML private Label lblMensajeReserva;

    @FXML private TableView<Reserva> tablaReservas;
    @FXML private TableColumn<Reserva, Long> colResId;
    @FXML private TableColumn<Reserva, String> colResPasajero;
    @FXML private TableColumn<Reserva, Integer> colResHabitacion;
    @FXML private TableColumn<Reserva, LocalDateTime> colResFecha;
    @FXML private TableColumn<Reserva, Integer> colResDias;
    @FXML private TableColumn<Reserva, LocalDateTime> colResCheckIn;
    @FXML private TableColumn<Reserva, LocalDateTime> colResCheckOut;
    @FXML private TableColumn<Reserva, EstadoReserva> colResEstado;

    /** TAB DE HABITACIONES */
    @FXML private DatePicker dpConsultaDesde;
    @FXML private TextField txtConsultaDias;
    @FXML private ComboBox<EstadoHabitacion> cmbEstadoHabitacion;
    @FXML private ComboBox<MotivoBloqueo> cmbMotivoBloqueo;
    @FXML private Label lblMensajeHabitacion;
    @FXML private ComboBox<EstadoHabitacion> cmbFiltroEstadoHab;

    @FXML private TableView<Habitacion> tablaHabitaciones;
    @FXML private TableColumn<Habitacion, Integer> colHabNumero;
    @FXML private TableColumn<Habitacion, TipoHabitacion> colHabTipo;
    @FXML private TableColumn<Habitacion, EstadoHabitacion> colHabEstado;
    @FXML private TableColumn<Habitacion, MotivoBloqueo> colHabMotivo;
    @FXML private TableColumn<Habitacion, String> colHabOcupante;
    @FXML private TableColumn<Habitacion, String> colHabDniOcupante;

    /** TAB DE PASAJEROS */
    @FXML private TextField txtPasNombre;
    @FXML private TextField txtPasApellido;
    @FXML private TextField txtPasDni;
    @FXML private TextField txtPasTelefono;
    @FXML private TextField txtPasEmail;
    @FXML private TextField txtPasOrigen;
    @FXML private TextField txtPasDomicilio;
    @FXML private Label lblMensajePasajero;
    @FXML private TextField txtBuscarPasajeroInforme;

    @FXML private TableView<Pasajero> tablaPasajeros;
    @FXML private TableColumn<Pasajero, String> colPasNombre;
    @FXML private TableColumn<Pasajero, String> colPasApellido;
    @FXML private TableColumn<Pasajero, String> colPasDni;
    @FXML private TableColumn<Pasajero, String> colPasOrigen;
    @FXML private TableColumn<Pasajero, String> colPasDomicilio;
    @FXML private TableColumn<Pasajero, String> colPasTelefono;

    private Pasajero pasajeroEncontradoParaReserva;
    private Habitacion habitacionSeleccionadaMapa;

    @FXML
    public void initialize() {
        colResId.setCellValueFactory(new PropertyValueFactory<>("reservaId"));
        colResPasajero.setCellValueFactory(new PropertyValueFactory<>("nombreCompletoPasajero"));
        colResHabitacion.setCellValueFactory(new PropertyValueFactory<>("numeroHabitacion"));
        colResFecha.setCellValueFactory(new PropertyValueFactory<>("diaReservado"));
        colResDias.setCellValueFactory(new PropertyValueFactory<>("duracionEstadia"));
        colResCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        colResCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        colResEstado.setCellValueFactory(new PropertyValueFactory<>("estadoReserva"));

        colHabNumero.setCellValueFactory(new PropertyValueFactory<>("numeroHabitacion"));
        colHabTipo.setCellValueFactory(new PropertyValueFactory<>("tipoHabitacion"));
        colHabEstado.setCellValueFactory(new PropertyValueFactory<>("estadoHabitacion"));
        colHabMotivo.setCellValueFactory(new PropertyValueFactory<>("motivoBloqueo"));
        colHabOcupante.setCellValueFactory(new PropertyValueFactory<>("ocupanteActual"));
        colHabDniOcupante.setCellValueFactory(new PropertyValueFactory<>("dniOcupanteActual"));

        colPasNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPasApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colPasDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colPasOrigen.setCellValueFactory(new PropertyValueFactory<>("origen"));
        colPasDomicilio.setCellValueFactory(new PropertyValueFactory<>("domicilioOrigen"));
        colPasTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        cmbEstadoHabitacion.getItems().setAll(EstadoHabitacion.values());
        cmbMotivoBloqueo.getItems().setAll(MotivoBloqueo.values());
        cmbFiltroEstadoHab.getItems().setAll(EstadoHabitacion.values());

        cargarTodasLasTablas();
    }

    private void cargarTodasLasTablas() {
        tablaReservas.setItems(FXCollections.observableArrayList(
                ReservaServicio.getInstancia().listar()
        ));
        tablaHabitaciones.setItems(FXCollections.observableArrayList(
                HabitacionServicio.getInstancia().listar()
        ));
        tablaPasajeros.setItems(FXCollections.observableArrayList(
                PasajeroServicio.getInstancia().listar()
        ));
    }

    @FXML
    public void buscarPasajeroPorDni() {
        String dni = txtDniPasajeroReserva.getText().trim();
        pasajeroEncontradoParaReserva = null;

        for (Pasajero p : PasajeroServicio.getInstancia().listar()) {
            if (p.getDni().equals(dni) && p.isActivo()) {
                pasajeroEncontradoParaReserva = p;
                break;
            }
        }

        if (pasajeroEncontradoParaReserva != null) {
            lblPasajeroSeleccionado.setText("Pasajero: " + pasajeroEncontradoParaReserva.getNombre() + " " + pasajeroEncontradoParaReserva.getApellido());
        } else {
            lblPasajeroSeleccionado.setText("Pasajero no encontrado.");
        }
    }

    @FXML
    public void actualizarHabitacionesDisponiblesReserva() {
        cmbHabitacionReserva.getItems().clear();
        LocalDate fecha = dpFechaIngreso.getValue();
        String textoDias = txtDuracionEstadia.getText().trim();

        if (fecha == null || textoDias.isEmpty()) return;

        try {
            int dias = Integer.parseInt(textoDias);
            if (dias <= 0) return;

            List<Integer> libres = new ArrayList<>();
            for (Habitacion h : HabitacionServicio.getInstancia().listar()) {
                if (habitacionDisponibleEnFechas(h, fecha, dias)) {
                    libres.add(h.getNumeroHabitacion());
                }
            }
            cmbHabitacionReserva.setItems(FXCollections.observableArrayList(libres));
        } catch (NumberFormatException ignored) {}
    }

    @FXML
    public void calcularPrecioSugerido() {
        Integer numHab = cmbHabitacionReserva.getValue();
        String textoDias = txtDuracionEstadia.getText().trim();
        if (numHab == null || textoDias.isEmpty()) return;

        try {
            int dias = Integer.parseInt(textoDias);
            Habitacion hab = buscarHabitacionPorNumero(numHab);
            if (hab != null && dias > 0) {
                double total = dias * obtenerTarifaDiaria(hab.getTipoHabitacion());
                txtPrecioReserva.setText(String.valueOf(total));
            }
        } catch (NumberFormatException ignored) {}
    }

    @FXML
    public void registrarReserva() {
        if (pasajeroEncontradoParaReserva == null) {
            lblMensajeReserva.setText("Busque un pasajero válido primero.");
            return;
        }

        LocalDate fecha = dpFechaIngreso.getValue();
        Integer numHab = cmbHabitacionReserva.getValue();

        if (fecha == null || numHab == null || txtDuracionEstadia.getText().trim().isEmpty() ||
                txtCantidadPasajeros.getText().trim().isEmpty() || txtPrecioReserva.getText().trim().isEmpty()) {
            lblMensajeReserva.setText("Complete todos los campos.");
            return;
        }

        try {
            int dias = Integer.parseInt(txtDuracionEstadia.getText().trim());
            int pasajeros = Integer.parseInt(txtCantidadPasajeros.getText().trim());
            double precio = Double.parseDouble(txtPrecioReserva.getText().trim());
            Habitacion habitacion = buscarHabitacionPorNumero(numHab);

            if (!habitacionDisponibleEnFechas(habitacion, fecha, dias)) {
                lblMensajeReserva.setText("Habitación no disponible en esas fechas.");
                return;
            }

            Reserva nueva = new Reserva();
            nueva.setPasajero(pasajeroEncontradoParaReserva);
            nueva.setHabitacion(habitacion);
            nueva.setDiaReservado(fecha.atStartOfDay());
            nueva.setDuracionEstadia(dias);
            nueva.setCantidadPasajeros(pasajeros);
            nueva.setPrecio(precio);
            nueva.setEstadoReserva(EstadoReserva.ACTIVA);

            ReservaServicio.getInstancia().agregar(nueva);
            cargarTodasLasTablas();
            limpiarFormularioReserva();
            lblMensajeReserva.setText("Reserva registrada con éxito.");
        } catch (Exception e) {
            lblMensajeReserva.setText(e.getMessage());
        }
    }

    @FXML
    public void registrarCheckIn() {
        Reserva sel = tablaReservas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Aviso", "Seleccione una reserva de la tabla.");
            return;
        }

        try {
            ReservaServicio.getInstancia().checkIn(sel.getReservaId());

            tablaReservas.refresh();
            tablaHabitaciones.refresh();
            mostrarAlerta("Check-In Exitoso", "Ingreso registrado en habitación " + sel.getNumeroHabitacion());
        } catch (HotelException e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    public void registrarCheckOut() {
        Reserva sel = tablaReservas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Aviso", "Seleccione una reserva de la tabla.");
            return;
        }

        try {
            ReservaServicio.getInstancia().checkOut(sel.getReservaId());

            tablaReservas.refresh();
            tablaHabitaciones.refresh();
            mostrarAlerta("Check-Out Exitoso", "Salida registrada. Total cobrado: ARS " + sel.getPrecio());
        } catch (HotelException e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }


    @FXML
    public void cancelarReserva() {
        Reserva sel = tablaReservas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Aviso", "Seleccione una reserva para cancelar.");
            return;
        }
        try {
            ReservaServicio.getInstancia().cancelar(sel.getReservaId());
            tablaReservas.refresh();
            mostrarAlerta("Cancelada", "Reserva cancelada correctamente.");
        } catch (HotelException e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    public void verificarDisponibilidadPorFechas() {
        cmbFiltroEstadoHab.setValue(null);
        LocalDate desde = dpConsultaDesde.getValue();
        String textoDias = txtConsultaDias.getText().trim();
        if (desde == null || textoDias.isEmpty()) return;

        try {
            int dias = Integer.parseInt(textoDias);
            List<Habitacion> libres = new ArrayList<>();
            for (Habitacion h : HabitacionServicio.getInstancia().listar()) {
                if (habitacionDisponibleEnFechas(h, desde, dias)) {
                    libres.add(h);
                }
            }
            tablaHabitaciones.setItems(FXCollections.observableArrayList(libres));
        } catch (NumberFormatException ignored) {}
    }

    @FXML
    public void seleccionarHabitacionTabla() {
        habitacionSeleccionadaMapa = tablaHabitaciones.getSelectionModel().getSelectedItem();
        if (habitacionSeleccionadaMapa != null) {
            cmbEstadoHabitacion.setValue(habitacionSeleccionadaMapa.getEstadoHabitacion());
            cmbMotivoBloqueo.setValue(habitacionSeleccionadaMapa.getMotivoBloqueo());
        }
    }

    @FXML
    public void actualizarEstadoHabitacion() {
        if (habitacionSeleccionadaMapa == null || cmbEstadoHabitacion.getValue() == null) return;
        try {
            if(cmbEstadoHabitacion.getValue() != EstadoHabitacion.NO_DISPONIBLE){
                cmbMotivoBloqueo.setValue(null);
            }
            habitacionSeleccionadaMapa.setEstadoHabitacion(cmbEstadoHabitacion.getValue());
            habitacionSeleccionadaMapa.setMotivoBloqueo(cmbMotivoBloqueo.getValue());
            HabitacionServicio.getInstancia().modificar(habitacionSeleccionadaMapa);
            tablaHabitaciones.refresh();
            lblMensajeHabitacion.setText("Habitación actualizada.");
        } catch (HotelException e) {
            lblMensajeHabitacion.setText(e.getMessage());
        }
    }

    @FXML
    public void filtrarMapaHabitaciones() {
        EstadoHabitacion filtro = cmbFiltroEstadoHab.getValue();
        if (filtro == null) {
            cargarTodasLasTablas();
            return;
        }
        List<Habitacion> filtradas = new ArrayList<>();
        for (Habitacion h : HabitacionServicio.getInstancia().listar()) {
            if (h.getEstadoHabitacion() == filtro) filtradas.add(h);
        }
        tablaHabitaciones.setItems(FXCollections.observableArrayList(filtradas));
    }

    @FXML
    public void guardarPasajeroRecepcion() {
        String dni = txtPasDni.getText().trim();
        try {
            Pasajero nuevo = new Pasajero(
                    txtPasNombre.getText().trim(), txtPasApellido.getText().trim(),
                    txtPasEmail.getText().trim(), dni, txtPasTelefono.getText().trim(),
                    dni, dni, txtPasOrigen.getText().trim(), txtPasDomicilio.getText().trim()
            );
            nuevo.setActivo(true);
            PasajeroServicio.getInstancia().agregar(nuevo);
            cargarTodasLasTablas();
            lblMensajePasajero.setText("Pasajero guardado (User/Pass: DNI).");
        } catch (HotelException e) {
            lblMensajePasajero.setText(e.getMessage());
        }
    }
    @FXML
    public void filtrarPasajerosInforme() {
        String txt = txtBuscarPasajeroInforme.getText().trim().toLowerCase();
        if (txt.isEmpty()) {
            cargarTodasLasTablas();
            return;
        }
        List filtrados = new ArrayList<>();
        for (Pasajero p : PasajeroServicio.getInstancia().listar()) {
            if (p.getNombre().toLowerCase().contains(txt) || p.getDni().contains(txt)) {
                filtrados.add(p);
            }
        }
        tablaPasajeros.setItems(FXCollections.observableArrayList(filtrados));
    }
    @FXML
    public void verHistorialPasajero() {
        Pasajero sel = tablaPasajeros.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        StringBuilder sb = new StringBuilder();
        for (Reserva r : ReservaServicio.getInstancia().listar()) {
            if (r.getPasajero() != null && r.getPasajero().getUsuarioId() == sel.getUsuarioId()) {
                sb.append("Hab ").append(r.getNumeroHabitacion()).append(" - ").append(r.getDiaReservado().toLocalDate()).append(" (").append(r.getEstadoReserva()).append(")\n");
            }
        }
        mostrarAlerta("Historial", sb.length() > 0 ? sb.toString() : "Sin reservas registradas.");
    }
    private Habitacion buscarHabitacionPorNumero(int numero) {
        for (Habitacion h : HabitacionServicio.getInstancia().listar()) {
            if (h.getNumeroHabitacion() == numero) return h;
        }
        return null;
    }
    private boolean habitacionDisponibleEnFechas(Habitacion hab, LocalDate inicioNuevo, int diasNuevos) {
        if (!hab.isActivo() || hab.getEstadoHabitacion() == EstadoHabitacion.NO_DISPONIBLE) return false;
        LocalDate finNuevo = inicioNuevo.plusDays(diasNuevos);
        for (Reserva r : ReservaServicio.getInstancia().listar()) {
            if (r.getHabitacion().getHabitacionId() != hab.getHabitacionId()) continue;
            if (r.getEstadoReserva() == EstadoReserva.CANCELADA) continue; ///CONTINUE ES COMO UN BREAK PERO NO SALE COMPLETO SINO QUE SOLO SALTEA LA VUELTA
            LocalDate iniEx = r.getDiaReservado().toLocalDate();
            LocalDate finEx = iniEx.plusDays(r.getDuracionEstadia());
            if (inicioNuevo.isBefore(finEx) && finNuevo.isAfter(iniEx)) return false;
        }
        return true;
    }
    private double obtenerTarifaDiaria(TipoHabitacion tipo) {
        if (tipo == null) return 0.0;
        switch (tipo) {
            case HABITACION_SIMPLE: return 50000.0;
            case HABITACION_DOBLE: return 100000;
            case SUITE: return 160000.0;
            default: return 0.0;
        }
    }
    private void limpiarFormularioReserva() {
        pasajeroEncontradoParaReserva = null;
        txtDniPasajeroReserva.clear();
        lblPasajeroSeleccionado.setText("Pasajero: No seleccionado");
        dpFechaIngreso.setValue(null);
        txtDuracionEstadia.clear();
        txtCantidadPasajeros.clear();
        cmbHabitacionReserva.getItems().clear();
        txtPrecioReserva.clear();
    }
    private void mostrarAlerta(String titulo, String msg) { ///CREA UNA VENTANITA PARA AVISAR LO Q VA OCURRIENDO
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
    @FXML
    public void cerrarSesion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tablaReservas.getScene().getWindow();
            stage.setScene(new Scene(root, 320, 420));
            stage.setTitle("Iniciar Sesión - Sistema Hotelero");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}