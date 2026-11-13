package com.hotel.interfaces;

import java.sql.SQLException;
import java.util.List;

/** Una interfaz diseñada para listar genéricamente, sin necesidad de tener un campo 'activo' */
public interface IListar<T> {
    List<T> listar() throws SQLException;
}
