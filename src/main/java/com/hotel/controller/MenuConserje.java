package com.hotel.controller;

import com.hotel.excepciones.HotelException;
import com.hotel.modelo.Habitacion;
import com.hotel.modelo.Pasajero;
import com.hotel.modelo.Reserva;
import com.hotel.modelo.enums.EstadoHabitacion;
import com.hotel.modelo.enums.EstadoReserva;
import com.hotel.modelo.enums.MotivoBloqueo;
import com.hotel.servicios.HabitacionServicio;
import com.hotel.servicios.PasajeroServicio;
import com.hotel.servicios.ReservaServicio;

import java.time.LocalDate;
import java.util.List;

public class MenuConserje {

    private final ReservaServicio reservaServicio;
    private final HabitacionServicio habitacionServicio;
    private final PasajeroServicio pasajeroServicio;

    public MenuConserje(ReservaServicio reservaServicio,
                        HabitacionServicio habitacionServicio,
                        PasajeroServicio pasajeroServicio) {
        this.reservaServicio = reservaServicio;
        this.habitacionServicio = habitacionServicio;
        this.pasajeroServicio = pasajeroServicio;
    }

    public void mostrar() {
        int opcion;
        do {
            System.out.println("\n--- PANEL CONSERJE / RECEPCIÓN ---");
            System.out.println("1. Reservas, Check-In y Check-Out");
            System.out.println("2. Estado, Disponibilidad y Bloqueo de Habitaciones");
            System.out.println("3. Pasajeros e Informes");
            System.out.println("0. Cerrar sesión");
            opcion = ConsolaUtil.leerEntero("Opción: ");

            switch (opcion) {
                case 1 -> gestionarReservas();
                case 2 -> gestionarHabitaciones();
                case 3 -> gestionarPasajeros();
                case 0 -> System.out.println("Sesión de conserje cerrada.");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private void gestionarReservas() {
        int opcion;
        do {
            System.out.println("\n--- RESERVAS, CHECK-IN Y CHECK-OUT ---");
            System.out.println("1. Crear nueva Reserva");
            System.out.println("2. Registrar Check-In");
            System.out.println("3. Registrar Check-Out");
            System.out.println("4. Modificar Reserva");
            System.out.println("5. Cancelar Reserva");
            System.out.println("6. Listar todas las Reservas / por estado");
            System.out.println("0. Volver");
            opcion = ConsolaUtil.leerEntero("Opción: ");

            try {
                switch (opcion) {
                    case 1 -> crearReserva();
                    case 2 -> {
                        reservaServicio.checkIn(ConsolaUtil.leerLong("ID de la reserva para Check-In: "));
                        System.out.println("Check-In registrado. Habitación marcada como OCUPADA.");
                    }
                    case 3 -> {
                        reservaServicio.checkOut(ConsolaUtil.leerLong("ID de la reserva para Check-Out: "));
                        System.out.println("Check-Out registrado. Reserva CUMPLIDA y habitación DISPONIBLE.");
                    }
                    case 4 -> {
                        long id = ConsolaUtil.leerLong("ID de la reserva a modificar: ");
                        Reserva actual = reservaServicio.buscarPorId(id);
                        if (actual == null) {
                            System.out.println("Reserva no encontrada.");
                            break;
                        }
                        int numHab = ConsolaUtil.leerEntero("Nuevo número de habitación: ");
                        Habitacion hab = habitacionServicio.buscarPorNumero(numHab);
                        if (hab == null) {
                            System.out.println("Habitación inexistente.");
                            break;
                        }
                        LocalDate fecha = ConsolaUtil.leerFecha("Nueva fecha de ingreso (YYYY-MM-DD): ");
                        int dias = ConsolaUtil.leerEntero("Nueva duración (días): ");
                        int cantPasajeros = ConsolaUtil.leerEntero("Cantidad de pasajeros: ");
                        double precio = ConsolaUtil.leerDouble("Nuevo precio: ");

                        Reserva mod = new Reserva(id, precio, cantPasajeros, actual.getEstadoReserva(), dias,
                                fecha.atStartOfDay(), null, null, hab, actual.getPasajero());
                        reservaServicio.modificar(mod);
                        System.out.println("Reserva modificada.");
                    }
                    case 5 -> {
                        reservaServicio.cancelar(ConsolaUtil.leerLong("ID de la reserva a cancelar: "));
                        System.out.println("Reserva cancelada.");
                    }
                    case 6 -> {
                        ConsolaUtil.imprimirLista(reservaServicio.listar(), "Todas las Reservas");
                        EstadoReserva est = ConsolaUtil.seleccionarEnum(EstadoReserva.class, "Filtrar por estado:");
                        ConsolaUtil.imprimirLista(reservaServicio.listarPorEstado(est), "Reservas " + est);
                    }
                }
            } catch (HotelException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void crearReserva() {
        String dni = ConsolaUtil.leerTexto("DNI del pasajero: ");
        Pasajero pasajero = pasajeroServicio.buscarPorDni(dni);
        if (pasajero == null) {
            System.out.println("No existe pasajero con ese DNI. Regístrelo primero.");
            return;
        }
        System.out.println("Pasajero: " + pasajero.getNombre() + " " + pasajero.getApellido());

        LocalDate desde = ConsolaUtil.leerFecha("Fecha de ingreso (YYYY-MM-DD): ");
        int dias = ConsolaUtil.leerEntero("Duración (días): ");

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

    private void gestionarHabitaciones() {
        int opcion;
        do {
            System.out.println("\n--- ESTADO DE HABITACIONES ---");
            System.out.println("1. Listar habitaciones disponibles hoy");
            System.out.println("2. Listar habitaciones ocupadas (con datos del ocupante)");
            System.out.println("3. Listar habitaciones bloqueadas / no disponibles (con motivo)");
            System.out.println("4. Consultar habitaciones disponibles en un período");
            System.out.println("5. Verificar si una habitación puede ocuparse en un período");
            System.out.println("6. Bloquear habitación");
            System.out.println("7. Liberar habitación bloqueada");
            System.out.println("0. Volver");
            opcion = ConsolaUtil.leerEntero("Opción: ");

            try {
                switch (opcion) {
                    case 1 -> {
                        habitacionServicio.actualizarEstadosPorFecha();
                        ConsolaUtil.imprimirLista(habitacionServicio.listarPorEstado(EstadoHabitacion.DISPONIBLE), "Disponibles Hoy");
                    }
                    case 2 -> listarOcupadasConOcupante();
                    case 3 -> ConsolaUtil.imprimirLista(habitacionServicio.listarPorEstado(EstadoHabitacion.NO_DISPONIBLE), "Habitaciones Bloqueadas");
                    case 4 -> {
                        LocalDate desde = ConsolaUtil.leerFecha("Fecha desde (YYYY-MM-DD): ");
                        int dias = ConsolaUtil.leerEntero("Cantidad de días: ");
                        ConsolaUtil.imprimirLista(habitacionServicio.listarDisponiblesEnPeriodo(desde, dias),
                                "Disponibles desde " + desde + " por " + dias + " días");
                    }
                    case 5 -> {
                        int num = ConsolaUtil.leerEntero("Número de habitación: ");
                        Habitacion h = habitacionServicio.buscarPorNumero(num);
                        if (h == null) {
                            System.out.println("No existe la habitación " + num);
                            break;
                        }
                        LocalDate desde = ConsolaUtil.leerFecha("Fecha desde (YYYY-MM-DD): ");
                        int dias = ConsolaUtil.leerEntero("Cantidad de días: ");
                        boolean libre = habitacionServicio.puedeOcupar(h.getHabitacionId(), desde, dias);
                        System.out.println(libre ? "SÍ se puede ocupar en ese período." : "NO está disponible en ese período.");
                    }
                    case 6 -> {
                        int num = ConsolaUtil.leerEntero("Número de habitación a bloquear: ");
                        Habitacion h = habitacionServicio.buscarPorNumero(num);
                        if (h == null) {
                            System.out.println("Habitación no encontrada.");
                            break;
                        }
                        MotivoBloqueo motivo = ConsolaUtil.seleccionarEnum(MotivoBloqueo.class, "Motivo de Bloqueo:");
                        habitacionServicio.bloquear(h.getHabitacionId(), motivo);
                        System.out.println("Habitación " + num + " bloqueada por " + motivo + ".");
                    }
                    case 7 -> {
                        int num = ConsolaUtil.leerEntero("Número de habitación a liberar: ");
                        Habitacion h = habitacionServicio.buscarPorNumero(num);
                        if (h == null) {
                            System.out.println("Habitación no encontrada.");
                            break;
                        }
                        habitacionServicio.liberar(h.getHabitacionId());
                        System.out.println("Habitación " + num + " liberada.");
                    }
                }
            } catch (HotelException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void listarOcupadasConOcupante() {
        List<Habitacion> ocupadas = habitacionServicio.listarPorEstado(EstadoHabitacion.OCUPADA);
        System.out.println("\n=== Habitaciones Ocupadas (" + ocupadas.size() + ") ===");
        if (ocupadas.isEmpty()) {
            System.out.println("(No hay habitaciones ocupadas actualmente)");
            return;
        }
        for (Habitacion h : ocupadas) {
            Pasajero ocupante = null;
            for (Reserva r : reservaServicio.listarPorHabitacion(h.getHabitacionId())) {
                if (r.getEstadoReserva() == EstadoReserva.ACTIVA && r.getCheckIn() != null && r.getCheckOut() == null) {
                    ocupante = r.getPasajero();
                    break;
                }
            }
            String info = (ocupante != null)
                    ? ocupante.getNombre() + " " + ocupante.getApellido() + " (DNI: " + ocupante.getDni() + ")"
                    : "Sin reserva activa asociada";
            System.out.println("Habitación " + h.getNumeroHabitacion() + " [" + h.getTipoHabitacion() + "] -> Ocupante: " + info);
        }
    }

    private void gestionarPasajeros() {
        int opcion;
        do {
            System.out.println("\n--- GESTIÓN DE PASAJEROS E INFORMES ---");
            System.out.println("1. Registrar nuevo Pasajero");
            System.out.println("2. Modificar Pasajero");
            System.out.println("3. Baja lógica de Pasajero");
            System.out.println("4. Reactivar Pasajero");
            System.out.println("5. Listar Pasajeros activos");
            System.out.println("6. Ver Informe e Historial de un Pasajero");
            System.out.println("0. Volver");
            opcion = ConsolaUtil.leerEntero("Opción: ");

            try {
                switch (opcion) {
                    case 1 -> {
                        String nombre = ConsolaUtil.leerTexto("Nombre: ");
                        String apellido = ConsolaUtil.leerTexto("Apellido: ");
                        String dni = ConsolaUtil.leerTexto("DNI: ");
                        String email = ConsolaUtil.leerTexto("Email: ");
                        String tel = ConsolaUtil.leerTexto("Teléfono: ");
                        String user = ConsolaUtil.leerTexto("Usuario: ");
                        String pass = ConsolaUtil.leerTexto("Contraseña: ");
                        String origen = ConsolaUtil.leerTexto("Origen: ");
                        String dom = ConsolaUtil.leerTexto("Domicilio: ");

                        Pasajero nuevo = new Pasajero(nombre, apellido, email, dni, tel, user, pass, origen, dom);
                        pasajeroServicio.agregar(nuevo);
                        System.out.println("Pasajero registrado con ID: " + nuevo.getUsuarioId());
                    }
                    case 2 -> {
                        long id = ConsolaUtil.leerLong("ID del pasajero a modificar: ");
                        Pasajero actual = pasajeroServicio.buscarPorId(id);
                        if (actual == null) {
                            System.out.println("Pasajero no encontrado.");
                            break;
                        }
                        String nombre = ConsolaUtil.leerTexto("Nombre (" + actual.getNombre() + "): ");
                        String apellido = ConsolaUtil.leerTexto("Apellido (" + actual.getApellido() + "): ");
                        String email = ConsolaUtil.leerTexto("Email (" + actual.getEmail() + "): ");
                        String dni = ConsolaUtil.leerTexto("DNI (" + actual.getDni() + "): ");
                        String tel = ConsolaUtil.leerTexto("Teléfono (" + actual.getTelefono() + "): ");
                        String user = ConsolaUtil.leerTexto("Usuario (" + actual.getNombreUsuario() + "): ");
                        String pass = ConsolaUtil.leerTexto("Contraseña: ");
                        String origen = ConsolaUtil.leerTexto("Origen (" + actual.getOrigen() + "): ");
                        String dom = ConsolaUtil.leerTexto("Domicilio (" + actual.getDomicilioOrigen() + "): ");

                        Pasajero mod = new Pasajero(id, nombre, apellido, email, dni, tel, user, pass, origen, dom);
                        pasajeroServicio.modificar(mod);
                        System.out.println("Pasajero actualizado.");
                    }
                    case 3 -> {
                        pasajeroServicio.baja(ConsolaUtil.leerLong("ID del pasajero a dar de baja: "));
                        System.out.println("Pasajero dado de baja.");
                    }
                    case 4 -> {
                        pasajeroServicio.reactivar(ConsolaUtil.leerLong("ID del pasajero a reactivar: "));
                        System.out.println("Pasajero reactivado.");
                    }
                    case 5 -> ConsolaUtil.imprimirLista(pasajeroServicio.listarActivos(), "Pasajeros Activos");
                    case 6 -> {
                        String dni = ConsolaUtil.leerTexto("DNI del pasajero: ");
                        Pasajero p = pasajeroServicio.buscarPorDni(dni);
                        if (p == null) {
                            System.out.println("No se encontró pasajero con DNI " + dni);
                            break;
                        }
                        System.out.println("\n" + pasajeroServicio.generarInformePasajero(p.getUsuarioId()));
                        ConsolaUtil.imprimirLista(reservaServicio.listarPorPasajero(p.getUsuarioId()), "Historial de Reservas");
                    }
                }
            } catch (HotelException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }
}