package com.hotel.controller;

import com.hotel.excepciones.HotelException;
import com.hotel.modelo.Administrador;
import com.hotel.modelo.Conserje;
import com.hotel.modelo.Pasajero;
import com.hotel.servicios.AdministradorServicio;
import com.hotel.servicios.ConserjeServicio;
import com.hotel.servicios.HabitacionServicio;
import com.hotel.servicios.PasajeroServicio;
import com.hotel.servicios.ReservaServicio;

public class MenuPrincipal {

    private final AdministradorServicio administradorServicio = AdministradorServicio.getInstancia();
    private final ConserjeServicio conserjeServicio = ConserjeServicio.getInstancia();
    private final PasajeroServicio pasajeroServicio = PasajeroServicio.getInstancia();
    private final HabitacionServicio habitacionServicio = HabitacionServicio.getInstancia();
    private final ReservaServicio reservaServicio = ReservaServicio.getInstancia();

    public void iniciar() {
        try {
            habitacionServicio.actualizarEstadosPorFecha();
        } catch (HotelException e) {
            System.out.println("Aviso inicial: " + e.getMessage());
        }

        int opcion;
        do {
            System.out.println("\n==========================================");
            System.out.println("     SISTEMA DE GESTIÓN HOTELERA");
            System.out.println("==========================================");
            System.out.println("1. Iniciar sesión");
            System.out.println("2. Registrarse como Pasajero");
            System.out.println("0. Salir");
            opcion = ConsolaUtil.leerEntero("Seleccione una opción: ");

            switch (opcion) {
                case 1 -> iniciarSesion();
                case 2 -> registrarPasajero();
                case 0 -> System.out.println("Cerrando sistema. ¡Hasta luego!");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 0);
    }

    private void iniciarSesion() {
        System.out.println("\n--- INICIO DE SESIÓN ---");
        String user = ConsolaUtil.leerTexto("Usuario: ");
        String pass = ConsolaUtil.leerTexto("Contraseña: ");

        Administrador admin = administradorServicio.obtenerAdministrador();
        if (admin != null && admin.isActivo()
                && admin.getNombreUsuario().equals(user)
                && admin.getContrasenia().equals(pass)) {
            System.out.println("\nBienvenido/a Administrador: " + admin.getNombre() + " " + admin.getApellido());
            new MenuAdministrador(administradorServicio, conserjeServicio, pasajeroServicio,
                    habitacionServicio, reservaServicio).mostrar();
            return;
        }

        for (Conserje c : conserjeServicio.listarActivos()) {
            if (c.getNombreUsuario().equals(user) && c.getContrasenia().equals(pass)) {
                System.out.println("\nBienvenido/a Conserje: " + c.getNombre() + " " + c.getApellido());
                new MenuConserje(reservaServicio, habitacionServicio, pasajeroServicio).mostrar();
                return;
            }
        }

        for (Pasajero p : pasajeroServicio.listarActivos()) {
            if (p.getNombreUsuario().equals(user) && p.getContrasenia().equals(pass)) {
                System.out.println("\nBienvenido/a Pasajero: " + p.getNombre() + " " + p.getApellido());
                new MenuPasajero(p, reservaServicio, habitacionServicio).mostrar();
                return;
            }
        }

        System.out.println("Credenciales incorrectas o usuario dado de baja.");
    }

    private void registrarPasajero() {
        System.out.println("\n--- REGISTRO DE PASAJERO ---");
        try {
            String nombre = ConsolaUtil.leerTexto("Nombre: ");
            String apellido = ConsolaUtil.leerTexto("Apellido: ");
            String dni = ConsolaUtil.leerTexto("DNI: ");
            String email = ConsolaUtil.leerTexto("Email: ");
            String telefono = ConsolaUtil.leerTexto("Teléfono: ");
            String usuario = ConsolaUtil.leerTexto("Nombre de usuario: ");
            String contrasenia = ConsolaUtil.leerTexto("Contraseña (mín. 4 caracteres): ");
            String origen = ConsolaUtil.leerTexto("Ciudad/País de origen: ");
            String domicilio = ConsolaUtil.leerTexto("Domicilio de origen: ");

            Pasajero nuevo = new Pasajero(nombre, apellido, email, dni, telefono, usuario, contrasenia, origen, domicilio);
            pasajeroServicio.agregar(nuevo);
            System.out.println("Cuenta creada con éxito. ID asignado: " + nuevo.getUsuarioId());
        } catch (HotelException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}