package com.hotel.controller;

import com.hotel.excepciones.HotelException;
import com.hotel.modelo.*;
import com.hotel.modelo.enums.TipoHabitacion;
import com.hotel.modelo.enums.Turno;
import com.hotel.servicios.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MenuAdministrador {

    private final AdministradorServicio administradorServicio;
    private final ConserjeServicio conserjeServicio;
    private final PasajeroServicio pasajeroServicio;
    private final HabitacionServicio habitacionServicio;
    private final ReservaServicio reservaServicio;

    public MenuAdministrador(AdministradorServicio administradorServicio,
                             ConserjeServicio conserjeServicio,
                             PasajeroServicio pasajeroServicio,
                             HabitacionServicio habitacionServicio,
                             ReservaServicio reservaServicio) {
        this.administradorServicio = administradorServicio;
        this.conserjeServicio = conserjeServicio;
        this.pasajeroServicio = pasajeroServicio;
        this.habitacionServicio = habitacionServicio;
        this.reservaServicio = reservaServicio;
    }

    public void mostrar() {
        int opcion;
        do {
            System.out.println("\n--- PANEL ADMINISTRADOR ---");
            System.out.println("1. Gestionar Conserjes (ABMCL)");
            System.out.println("2. Gestionar Habitaciones (ABMCL)");
            System.out.println("3. Modificar datos del Administrador");
            System.out.println("4. Generar Backup de la Base de Datos (.sql)");
            System.out.println("0. Cerrar sesión");
            opcion = ConsolaUtil.leerEntero("Opción: ");

            switch (opcion) {
                case 1 -> gestionarConserjes();
                case 2 -> gestionarHabitaciones();
                case 3 -> modificarAdministrador();
                case 4 -> generarBackup();
                case 0 -> System.out.println("Sesión de administrador cerrada.");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private void gestionarConserjes() {
        int opcion;
        do {
            System.out.println("\n--- GESTIÓN DE CONSERJES ---");
            System.out.println("1. Alta de Conserje");
            System.out.println("2. Modificar Conserje");
            System.out.println("3. Baja lógica de Conserje");
            System.out.println("4. Reactivar Conserje");
            System.out.println("5. Listar todos / activos / inactivos");
            System.out.println("6. Listar Conserjes activos por turno");
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
                        String user = ConsolaUtil.leerTexto("Nombre de usuario: ");
                        String pass = ConsolaUtil.leerTexto("Contraseña: ");
                        double salario = ConsolaUtil.leerDouble("Salario: ");
                        Turno turno = ConsolaUtil.seleccionarEnum(Turno.class, "Seleccione Turno:");

                        Conserje c = new Conserje(nombre, apellido, email, dni, tel, user, pass, salario, turno);
                        conserjeServicio.agregar(c);
                        System.out.println("Conserje registrado con ID: " + c.getUsuarioId());
                    }
                    case 2 -> {
                        long id = ConsolaUtil.leerLong("ID del conserje a modificar: ");
                        Conserje actual = conserjeServicio.buscarPorId(id);
                        if (actual == null) {
                            System.out.println("Conserje no encontrado.");
                            break;
                        }
                        String nombre = ConsolaUtil.leerTexto("Nombre (" + actual.getNombre() + "): ");
                        String apellido = ConsolaUtil.leerTexto("Apellido (" + actual.getApellido() + "): ");
                        String dni = ConsolaUtil.leerTexto("DNI (" + actual.getDni() + "): ");
                        String email = ConsolaUtil.leerTexto("Email (" + actual.getEmail() + "): ");
                        String tel = ConsolaUtil.leerTexto("Teléfono (" + actual.getTelefono() + "): ");
                        String user = ConsolaUtil.leerTexto("Usuario (" + actual.getNombreUsuario() + "): ");
                        String pass = ConsolaUtil.leerTexto("Contraseña: ");
                        double salario = ConsolaUtil.leerDouble("Salario (" + actual.getSalario() + "): ");
                        Turno turno = ConsolaUtil.seleccionarEnum(Turno.class, "Seleccione Turno:");

                        Conserje mod = new Conserje(id, nombre, apellido, email, dni, tel, user, pass, salario, turno);
                        conserjeServicio.modificar(mod);
                        System.out.println("Conserje actualizado.");
                    }
                    case 3 -> {
                        conserjeServicio.baja(ConsolaUtil.leerLong("ID del conserje a dar de baja: "));
                        System.out.println("Conserje dado de baja.");
                    }
                    case 4 -> {
                        conserjeServicio.reactivar(ConsolaUtil.leerLong("ID del conserje a reactivar: "));
                        System.out.println("Conserje reactivado.");
                    }
                    case 5 -> {
                        ConsolaUtil.imprimirLista(conserjeServicio.listar(), "Todos los Conserjes");
                        ConsolaUtil.imprimirLista(conserjeServicio.listarActivos(), "Conserjes Activos");
                        ConsolaUtil.imprimirLista(conserjeServicio.listarInactivos(), "Conserjes Inactivos");
                    }
                    case 6 -> {
                        Turno turno = ConsolaUtil.seleccionarEnum(Turno.class, "Seleccione Turno:");
                        ConsolaUtil.imprimirLista(conserjeServicio.listarPorTurno(turno), "Conserjes turno " + turno);
                    }
                }
            } catch (HotelException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void gestionarHabitaciones() {
        int opcion;
        do {
            System.out.println("\n--- GESTIÓN DE HABITACIONES ---");
            System.out.println("1. Alta de Habitación");
            System.out.println("2. Modificar Habitación");
            System.out.println("3. Baja lógica de Habitación");
            System.out.println("4. Reactivar Habitación");
            System.out.println("5. Listar todas las Habitaciones");
            System.out.println("0. Volver");
            opcion = ConsolaUtil.leerEntero("Opción: ");

            try {
                switch (opcion) {
                    case 1 -> {
                        int numero = ConsolaUtil.leerEntero("Número de habitación: ");
                        TipoHabitacion tipo = ConsolaUtil.seleccionarEnum(TipoHabitacion.class, "Tipo de Habitación:");
                        Habitacion h = new Habitacion(numero, tipo);
                        habitacionServicio.agregar(h);
                        System.out.println("Habitación creada con ID: " + h.getHabitacionId());
                    }
                    case 2 -> {
                        long id = ConsolaUtil.leerLong("ID de la habitación a modificar: ");
                        Habitacion actual = habitacionServicio.buscarPorId(id);
                        if (actual == null) {
                            System.out.println("Habitación no encontrada.");
                            break;
                        }
                        int numero = ConsolaUtil.leerEntero("Nuevo número (" + actual.getNumeroHabitacion() + "): ");
                        TipoHabitacion tipo = ConsolaUtil.seleccionarEnum(TipoHabitacion.class, "Nuevo Tipo:");
                        Habitacion mod = new Habitacion(id, numero, tipo, actual.getEstadoHabitacion(),
                                actual.getMotivoBloqueo(), actual.isActivo());
                        habitacionServicio.modificar(mod);
                        System.out.println("Habitación actualizada.");
                    }
                    case 3 -> {
                        habitacionServicio.baja(ConsolaUtil.leerLong("ID de la habitación a dar de baja: "));
                        System.out.println("Habitación dada de baja.");
                    }
                    case 4 -> {
                        habitacionServicio.reactivar(ConsolaUtil.leerLong("ID de la habitación a reactivar: "));
                        System.out.println("Habitación reactivada.");
                    }
                    case 5 -> ConsolaUtil.imprimirLista(habitacionServicio.listar(), "Inventario de Habitaciones");
                }
            } catch (HotelException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void modificarAdministrador() {
        try {
            Administrador actual = administradorServicio.obtenerAdministrador();
            if (actual == null) {
                System.out.println("No se encontró el administrador en el sistema.");
                return;
            }
            System.out.println("\n--- MODIFICAR ADMINISTRADOR (" + actual.getNombreUsuario() + ") ---");
            String nombre = ConsolaUtil.leerTexto("Nombre: ");
            String apellido = ConsolaUtil.leerTexto("Apellido: ");
            String email = ConsolaUtil.leerTexto("Email: ");
            String dni = ConsolaUtil.leerTexto("DNI: ");
            String tel = ConsolaUtil.leerTexto("Teléfono: ");
            String user = ConsolaUtil.leerTexto("Nombre de usuario: ");
            String pass = ConsolaUtil.leerTexto("Contraseña: ");

            Administrador mod = new Administrador(actual.getUsuarioId(), nombre, apellido, email, dni, tel, user, pass);
            administradorServicio.modificar(mod);
            System.out.println("Datos del administrador actualizados.");
        } catch (HotelException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void generarBackup() {
        String nombreArchivo = "backup_hotel_" + LocalDate.now() + ".sql";
        File archivoDestino = new File(nombreArchivo);

        List<Usuario> todosLosUsuarios = new ArrayList<>();
        Administrador admin = administradorServicio.obtenerAdministrador();
        if (admin != null) {
            todosLosUsuarios.add(admin);
        }
        todosLosUsuarios.addAll(conserjeServicio.listar());
        todosLosUsuarios.addAll(pasajeroServicio.listar());

        try (PrintWriter writer = new PrintWriter(new FileWriter(archivoDestino))) {
            writer.println("-- BACKUP DEL SISTEMA HOTELERO");
            writer.println("-- Fecha: " + LocalDateTime.now());
            writer.println();

            writer.println("-- TABLA USUARIO");
            for (Usuario u : todosLosUsuarios) {
                writer.println("INSERT INTO Usuario (usuarioId, nombre, apellido, email, dni, telefono, nombreUsuario, contrasenia, activo, tipoUsuario) VALUES (" +
                        u.getUsuarioId() + ", '" + u.getNombre() + "', '" + u.getApellido() + "', '" +
                        u.getEmail() + "', '" + u.getDni() + "', '" + u.getTelefono() + "', '" +
                        u.getNombreUsuario() + "', '" + u.getContrasenia() + "', " + u.isActivo() + ", '" +
                        u.getTipoUsuario() + "');");
            }
            writer.println();

            writer.println("-- TABLA CONSERJE");
            for (Conserje c : conserjeServicio.listar()) {
                writer.println("INSERT INTO Conserje (usuarioId, salario, turno) VALUES (" +
                        c.getUsuarioId() + ", " + c.getSalario() + ", '" + c.getTurno() + "');");
            }
            writer.println();

            writer.println("-- TABLA PASAJERO");
            for (Pasajero p : pasajeroServicio.listar()) {
                writer.println("INSERT INTO Pasajero (usuarioId, origen, domicilioOrigen) VALUES (" +
                        p.getUsuarioId() + ", '" + p.getOrigen() + "', '" + p.getDomicilioOrigen() + "');");
            }
            writer.println();

            writer.println("-- TABLA HABITACION");
            for (Habitacion h : habitacionServicio.listar()) {
                String motivoSql = (h.getMotivoBloqueo() == null) ? "NULL" : "'" + h.getMotivoBloqueo() + "'";
                writer.println("INSERT INTO Habitacion (habitacionId, numeroHabitacion, tipoHabitacion, estadoHabitacion, motivoBloqueo, activo) VALUES (" +
                        h.getHabitacionId() + ", " + h.getNumeroHabitacion() + ", '" + h.getTipoHabitacion() + "', '" +
                        h.getEstadoHabitacion() + "', " + motivoSql + ", " + h.isActivo() + ");");
            }
            writer.println();

            writer.println("-- TABLA RESERVA");
            for (Reserva r : reservaServicio.listar()) {
                String checkInSql = (r.getCheckIn() == null) ? "NULL" : "'" + r.getCheckIn() + "'";
                String checkOutSql = (r.getCheckOut() == null) ? "NULL" : "'" + r.getCheckOut() + "'";
                writer.println("INSERT INTO Reserva (reservaId, precio, cantidadPasajeros, estadoReserva, duracionEstadia, diaReservado, checkIn, checkOut, habitacion, pasajero) VALUES (" +
                        r.getReservaId() + ", " + r.getPrecio() + ", " + r.getCantidadPasajeros() + ", '" +
                        r.getEstadoReserva() + "', " + r.getDuracionEstadia() + ", '" + r.getDiaReservado() + "', " +
                        checkInSql + ", " + checkOutSql + ", " + r.getHabitacion().getHabitacionId() + ", " +
                        r.getPasajero().getUsuarioId() + ");");
            }

            System.out.println("Backup generado correctamente en: " + archivoDestino.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error al generar el archivo de backup: " + e.getMessage());
        }
    }
}