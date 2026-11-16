package com.hotel.controller;

import com.hotel.excepciones.HotelException;
import com.hotel.modelo.Habitacion;
import com.hotel.modelo.Pasajero;
import com.hotel.modelo.Reserva;
import com.hotel.servicios.HabitacionServicio;
import com.hotel.servicios.ReservaServicio;

import java.time.LocalDate;
import java.util.List;

public class MenuPasajero {

    private final Pasajero pasajero;
    private final ReservaServicio reservaServicio;
    private final HabitacionServicio habitacionServicio;

    public MenuPasajero(Pasajero pasajero,
                        ReservaServicio reservaServicio,
                        HabitacionServicio habitacionServicio) {
        this.pasajero = pasajero;
        this.reservaServicio = reservaServicio;
        this.habitacionServicio = habitacionServicio;
    }

    public void mostrar() {
        int opcion;
        do {
            System.out.println("\n--- PANEL PASAJERO ---");
            System.out.println("1. Consultar habitaciones disponibles en un período");
            System.out.println("2. Realizar una reserva");
            System.out.println("3. Mis reservas / historial");
            System.out.println("4. Cancelar una reserva pendiente");
            System.out.println("0. Cerrar sesión");
            opcion = ConsolaUtil.leerEntero("Opción: ");

            try {
                switch (opcion) {
                    case 1 -> {
                        LocalDate desde = ConsolaUtil.leerFecha("Fecha de ingreso (YYYY-MM-DD): ");
                        int dias = ConsolaUtil.leerEntero("Cantidad de noches: ");
                        ConsolaUtil.imprimirLista(habitacionServicio.listarDisponiblesEnPeriodo(desde, dias),
                                "Habitaciones Disponibles");
                    }
                    case 2 -> crearReservaPropia();
                    case 3 -> ConsolaUtil.imprimirLista(reservaServicio.listarPorPasajero(pasajero.getUsuarioId()),
                            "Mis Reservas");
                    case 4 -> {
                        long reservaId = ConsolaUtil.leerLong("ID de la reserva a cancelar: ");
                        Reserva r = reservaServicio.buscarPorId(reservaId);
                        if (r == null || r.getPasajero().getUsuarioId() != pasajero.getUsuarioId()) {
                            System.out.println("No se encontró una reserva suya con ese ID.");
                            break;
                        }
                        reservaServicio.cancelar(reservaId);
                        System.out.println("Reserva cancelada con éxito.");
                    }
                    case 0 -> System.out.println("Sesión cerrada.");
                    default -> System.out.println("Opción inválida.");
                }
            } catch (HotelException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void crearReservaPropia() {
        LocalDate desde = ConsolaUtil.leerFecha("Fecha de ingreso (YYYY-MM-DD): ");
        int dias = ConsolaUtil.leerEntero("Cantidad de noches: ");

        List<Habitacion> disponibles = habitacionServicio.listarDisponiblesEnPeriodo(desde, dias);
        if (disponibles.isEmpty()) {
            System.out.println("No hay habitaciones disponibles para esas fechas.");
            return;
        }
        ConsolaUtil.imprimirLista(disponibles, "Habitaciones Disponibles");

        int numHab = ConsolaUtil.leerEntero("Número de habitación elegida: ");
        Habitacion habitacion = habitacionServicio.buscarPorNumero(numHab);
        if (habitacion == null) {
            System.out.println("Número de habitación inválido.");
            return;
        }

        int cantPasajeros = ConsolaUtil.leerEntero("Cantidad de pasajeros: ");
        double precio = ConsolaUtil.leerDouble("Precio total ($): ");

        Reserva nueva = new Reserva(precio, cantPasajeros, dias, desde.atStartOfDay(), habitacion, pasajero);
        reservaServicio.agregar(nueva);
        System.out.println("¡Reserva confirmada! ID: " + nueva.getReservaId());
    }
}