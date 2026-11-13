package com.hotel.interfaces;

import java.sql.SQLException;
import java.util.List;

/** Una interfaz diseñada únicamente para mostrar una lista de elementos con baja lógica */
public interface IListarPorEstado<T> {
    List<T> listarActivos() throws SQLException;
    List<T> listarInactivos() throws SQLException;
}
