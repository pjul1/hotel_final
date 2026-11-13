package com.hotel.interfaces;

import java.sql.SQLException;

/** Interfaz especifica para elementos con baja lógica. */
public interface IBajaLogica {
    void baja (long id) throws SQLException;
    void reactivar(long id) throws SQLException;
}
