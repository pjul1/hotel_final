package com.hotel.dao;

import java.util.List;

public interface ICrud<T> {
    void agregar(T t);
    void modificar(T t);
    void baja (long id);
    T buscarPorId(long id);
    List<T> listar();
}
