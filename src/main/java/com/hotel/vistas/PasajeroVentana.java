package com.hotel.vistas;

import com.hotel.excepciones.HotelException;
import com.hotel.modelo.Habitacion;
import com.hotel.modelo.Pasajero;
import com.hotel.modelo.Reserva;
import com.hotel.modelo.enums.EstadoHabitacion;
import com.hotel.modelo.enums.EstadoReserva;
import com.hotel.modelo.enums.TipoHabitacion;
import com.hotel.servicios.HabitacionServicio;
import com.hotel.servicios.ReservaServicio;
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

public class PasajeroVentana {

    /** TAB DE RESERVAS DEL PASAJERO */
    @FXML private DatePicker dpFechaIngreso;
    @FXML private TextField txtDuracionEstadia;
    @FXML private TextField txtCantidadPasajeros;
    @FXML private ComboBox<TipoHabitacion> cmbTipoHabitacion;
    @FXML private ComboBox<Integer> cmbHabitacionDisponible;
    @FXML private TableColumn<Reserva, Long> colReservaId;
    @FXML private TableColumn<Reserva, Integer> colHabitacion;
    @FXML private TableColumn<Reserva, LocalDateTime> colFechaReservada;
    @FXML private TableColumn<Reserva, Integer> colDias;
    @FXML private TableColumn<Reserva, Integer> colPasajeros;
    @FXML private TableColumn<Reserva, Double> colPrecio;
    @FXML private TableColumn<Reserva, EstadoReserva> colEstadoReserva;
    @FXML private TableView<Reserva> tablaReservas;
    @FXML private Label lblPrecioTotal;
    @FXML private Label lblMensajeReserva;
    private Pasajero pasajeroLogueado;

    public void setPasajeroLogueado(Pasajero pasajero) { ///SIRVE PARA SABER EL PASAJERO LOGUEADO Y ADAPTAR LA INTERFAZ PARA EL
        this.pasajeroLogueado = pasajero;
        cargarTablaReservas();
    }

    @FXML
    public void initialize(){
        colReservaId.setCellValueFactory(new PropertyValueFactory<>("reservaId"));
        colHabitacion.setCellValueFactory(new PropertyValueFactory<>("numeroHabitacion"));
        colFechaReservada.setCellValueFactory(new PropertyValueFactory<>("diaReservado"));
        colDias.setCellValueFactory(new PropertyValueFactory<>("duracionEstadia"));
        colPasajeros.setCellValueFactory(new PropertyValueFactory<>("cantidadPasajeros"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colEstadoReserva.setCellValueFactory(new PropertyValueFactory<>("estadoReserva"));

        cargarTablaReservas();

        cmbTipoHabitacion.getItems().setAll(TipoHabitacion.values());
    }

    private void cargarTablaReservas() {
        if (pasajeroLogueado != null) {
            tablaReservas.setItems(FXCollections.observableArrayList(
                    ReservaServicio.getInstancia().listarPorPasajero(pasajeroLogueado.getUsuarioId())
            ));
        } else {
            tablaReservas.setItems(FXCollections.observableArrayList(
                    ReservaServicio.getInstancia().listar()
            ));
        }
    }


    @FXML
    public void actualizarDisponibilidadYPrecio() {
        cmbHabitacionDisponible.getItems().clear();
        lblPrecioTotal.setText("ARS 0.00");

        LocalDate fechaInicio = dpFechaIngreso.getValue(); ///USO LOCALDATE PORQUE DATAPICKER DEVUELVE LOCALDATE
        TipoHabitacion tipo = cmbTipoHabitacion.getValue();
        String textoDias = txtDuracionEstadia.getText().trim();

        if (fechaInicio == null || tipo == null || textoDias.isEmpty()) {
            return;
        }

        int dias;
        try {
            dias = Integer.parseInt(textoDias);
            if (dias <= 0) return;
        } catch (NumberFormatException e) {
            return;
        }

        double total = dias * obtenerTarifaDiaria(tipo);
        lblPrecioTotal.setText(String.format("ARS %.2f", total));

        List<Integer> numerosDisponibles = new ArrayList<>();
        for (Habitacion h : HabitacionServicio.getInstancia().listar()) {
            if (h.getTipoHabitacion() == tipo && habitacionDisponibleEnFechas(h, fechaInicio, dias)) {
                numerosDisponibles.add(h.getNumeroHabitacion());
            }
        }

        cmbHabitacionDisponible.setItems(FXCollections.observableArrayList(numerosDisponibles));
    }

    /** Compobamos que el rango solicitado no se solape con reservas activas. */
    private boolean habitacionDisponibleEnFechas(Habitacion habitacion, LocalDate inicioNuevo, int diasNuevos) {
        if (!habitacion.isActivo() || habitacion.getEstadoHabitacion() == EstadoHabitacion.NO_DISPONIBLE) {
            return false;
        }

        LocalDate finNuevo = inicioNuevo.plusDays(diasNuevos); /** plusdays suma una cantidad de días a una fecha y te devuelve la fecha resultante */

        for (Reserva r : ReservaServicio.getInstancia().listar()) {
            if (r.getHabitacion().getHabitacionId() != habitacion.getHabitacionId()) {
                continue;
            }
            if (r.getEstadoReserva() == EstadoReserva.CANCELADA) {
                continue;
            }

            LocalDate inicioExistente = r.getDiaReservado().toLocalDate();
            LocalDate finExistente = inicioExistente.plusDays(r.getDuracionEstadia());

            boolean seSolapan = inicioNuevo.isBefore(finExistente) && finNuevo.isAfter(inicioExistente);
            if (seSolapan) {
                return false;
            }
        }
        return true;
    }

    @FXML
    public void confirmarReserva() {
        lblMensajeReserva.setText("");

        LocalDate fechaInicio = dpFechaIngreso.getValue();
        TipoHabitacion tipo = cmbTipoHabitacion.getValue();
        Integer numeroHabitacionElegido = cmbHabitacionDisponible.getValue();

        if (fechaInicio == null || tipo == null || numeroHabitacionElegido == null ||
                txtDuracionEstadia.getText().trim().isEmpty() || txtCantidadPasajeros.getText().trim().isEmpty()) {
            lblMensajeReserva.setText("Complete todos los campos y elija una habitación disponible");
            return;
        }

        if (fechaInicio.isBefore(LocalDate.now())) {
            lblMensajeReserva.setText("La fecha de ingreso no puede ser anterior a hoy");
            return;
        }

        Habitacion habitacionElegida = null;
        for (Habitacion h : HabitacionServicio.getInstancia().listar()) {
            if (h.getNumeroHabitacion() == numeroHabitacionElegido) {
                habitacionElegida = h;
                break;
            }
        }

        if (habitacionElegida == null) {
            lblMensajeReserva.setText("No se encontró la habitación seleccionada");
            return;
        }

        try {
            int dias = Integer.parseInt(txtDuracionEstadia.getText().trim());
            int pasajeros = Integer.parseInt(txtCantidadPasajeros.getText().trim());

            if (!habitacionDisponibleEnFechas(habitacionElegida, fechaInicio, dias)) {
                lblMensajeReserva.setText("La habitación seleccionada ya no está disponible en esas fechas");
                actualizarDisponibilidadYPrecio();
                return;
            }

            double precioFinal = dias * obtenerTarifaDiaria(tipo);

            Reserva nuevaReserva = new Reserva();
            nuevaReserva.setDiaReservado(fechaInicio.atStartOfDay()); ///SIRVE PARA CONVERTIR LA FECHA SIN HORA EN UNA CON HORA
            nuevaReserva.setDuracionEstadia(dias);
            nuevaReserva.setCantidadPasajeros(pasajeros);
            nuevaReserva.setPrecio(precioFinal);
            nuevaReserva.setEstadoReserva(EstadoReserva.ACTIVA);
            nuevaReserva.setHabitacion(habitacionElegida);
            nuevaReserva.setPasajero(pasajeroLogueado);

            ReservaServicio.getInstancia().agregar(nuevaReserva);

            cargarTablaReservas();
            limpiarFormulario();
            lblMensajeReserva.setText("¡Reserva confirmada con éxito!");

        } catch (NumberFormatException e) {
            lblMensajeReserva.setText("Días y pasajeros deben ser números válidos");
        } catch (HotelException e) {
            lblMensajeReserva.setText(e.getMessage());
        }
    }

    @FXML
    public void cancelarReservaSeleccionada() {
        Reserva seleccionada = tablaReservas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            lblMensajeReserva.setText("Seleccione una reserva de la tabla para cancelar");
            return;
        }

        try {
            ReservaServicio.getInstancia().cancelar(seleccionada.getReservaId());
            tablaReservas.refresh();
            lblMensajeReserva.setText("Reserva cancelada correctamente");
        } catch (HotelException e) {
            lblMensajeReserva.setText(e.getMessage());
        }
    }

    @FXML
    public void cerrarSesion(){
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

    private double obtenerTarifaDiaria(TipoHabitacion tipo) {
        switch (tipo) {
            case HABITACION_SIMPLE: return 50000.0;
            case HABITACION_DOBLE: return 100000.0;
            case SUITE: return 160000.0;
            default: return 0.0;
        }
    }

    private void limpiarFormulario() {
        dpFechaIngreso.setValue(null);
        txtDuracionEstadia.clear();
        txtCantidadPasajeros.clear();
        cmbTipoHabitacion.setValue(null);
        cmbHabitacionDisponible.getItems().clear();
        lblPrecioTotal.setText("ARS 0.00");
    }
}
