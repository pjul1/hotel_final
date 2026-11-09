package com.hotel.interfaces;

import java.util.List;

public interface ICrud <T>{
    void agregar(T t);
    void modificar(T t);
    void baja(long id);
    T buscar(long id);
    List<T> listar();
}
