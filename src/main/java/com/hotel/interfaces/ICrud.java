package com.hotel.interfaces;

import java.sql.SQLException;

/** Interfaz que implementa los métodos CRUD */
public interface ICrud<T> {
    void agregar(T t) throws SQLException;
    void modificar(T t) throws SQLException;
    T buscarPorId(long id) throws SQLException;
}
