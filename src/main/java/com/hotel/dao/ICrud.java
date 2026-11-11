package com.hotel.dao;

import java.sql.SQLException;
import java.util.List;

public interface ICrud<T> {
    void agregar(T t) throws SQLException;
    void modificar(T t) throws SQLException;
    void baja (long id) throws SQLException;
    T buscarPorId(long id) throws SQLException;
    List<T> listar() throws SQLException;
}
