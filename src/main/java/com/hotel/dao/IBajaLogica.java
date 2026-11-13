package com.hotel.dao;

import java.sql.SQLException;
import java.util.List;

public interface IBajaLogica<T> {
    List<T> listarActivos() throws SQLException;
    void reactivar(long id) throws SQLException;
}
