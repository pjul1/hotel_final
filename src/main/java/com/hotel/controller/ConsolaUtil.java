package com.hotel.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public final class ConsolaUtil {

    private static final Scanner SCANNER = new Scanner(System.in);

    private ConsolaUtil() {
    }

    public static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return SCANNER.nextLine().trim();
    }

    public static int leerEntero(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                return Integer.parseInt(SCANNER.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número entero válido.");
            }
        }
    }

    public static long leerLong(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                return Long.parseLong(SCANNER.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un ID numérico válido.");
            }
        }
    }

    public static double leerDouble(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                return Double.parseDouble(SCANNER.nextLine().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un monto numérico válido.");
            }
        }
    }

    public static LocalDate leerFecha(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                return LocalDate.parse(SCANNER.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido. Use AAAA-MM-DD (ej: 2026-11-20).");
            }
        }
    }

    public static <E extends Enum<E>> E seleccionarEnum(Class<E> enumClass, String titulo) {
        E[] valores = enumClass.getEnumConstants();
        while (true) {
            System.out.println(titulo);
            for (int i = 0; i < valores.length; i++) {
                System.out.println((i + 1) + ". " + valores[i]);
            }
            int idx = leerEntero("Opción: ") - 1;
            if (idx >= 0 && idx < valores.length) {
                return valores[idx];
            }
            System.out.println("Selección inválida.");
        }
    }

    public static <T> void imprimirLista(List<T> lista, String titulo) {
        System.out.println("\n=== " + titulo + " (" + lista.size() + ") ===");
        if (lista.isEmpty()) {
            System.out.println("(Sin registros)");
            return;
        }
        for (T item : lista) {
            System.out.println(" - " + item);
        }
    }
}