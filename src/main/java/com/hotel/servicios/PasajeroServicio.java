package com.hotel.servicios;

import com.hotel.dao.PasajeroDao;
import com.hotel.excepciones.HotelException;
import com.hotel.interfaces.IBajaLogica;
import com.hotel.interfaces.ICrud;
import com.hotel.interfaces.IListar;
import com.hotel.interfaces.IListarPorEstado;
import com.hotel.modelo.Pasajero;
import com.hotel.modelo.enums.TipoUsuario;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lógica de negocio de los pasajeros.
 * Implementa las mismas interfaces que PasajeroDao y aplica la Alternativa B:
 * mantiene un Map en memoria sincronizado con MySQL para resolver consultas, filtros
 * y búsquedas sin consultar continuamente la base de datos.
 */
public class PasajeroServicio implements ICrud<Pasajero>, IBajaLogica, IListar<Pasajero>, IListarPorEstado<Pasajero> {

    private static PasajeroServicio instancia;

    private final PasajeroDao pasajeroDao = new PasajeroDao();

    /** Colección en memoria indexada por usuarioId (garantiza unicidad y búsqueda O(1)). */
    private final Map<Long, Pasajero> pasajerosPorId = new LinkedHashMap<>();

    /** Carga inicial de los pasajeros desde MySQL hacia la colección en memoria. */
    private PasajeroServicio() {
        cargarDesdeBase();
    }

    public static PasajeroServicio getInstancia() {
        if (instancia == null) {
            instancia = new PasajeroServicio();
        }
        return instancia;
    }

    /**
     * Valida y registra un nuevo pasajero en MySQL y, si tiene éxito,
     * lo agrega directamente al Map en memoria (Alternativa B).
     */
    @Override
    public void agregar(Pasajero pasajero) {
        validarPasajero(pasajero);
        verificarDniDisponible(pasajero.getDni(), 0);
        verificarNombreUsuarioDisponible(pasajero.getNombreUsuario(), 0);

        pasajero.setActivo(true);
        pasajero.setTipoUsuario(TipoUsuario.PASAJERO);

        try {
            pasajeroDao.agregar(pasajero);
            pasajerosPorId.put(pasajero.getUsuarioId(), pasajero);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("registrar el pasajero", e);
        }
    }

    /**
     * Valida y actualiza un pasajero existente en MySQL y sincroniza el objeto en el Map en memoria.
     */
    @Override
    public void modificar(Pasajero pasajero) {
        validarPasajero(pasajero);
        Pasajero actual = obtenerExistente(pasajero.getUsuarioId());

        verificarDniDisponible(pasajero.getDni(), pasajero.getUsuarioId());
        verificarNombreUsuarioDisponible(pasajero.getNombreUsuario(), pasajero.getUsuarioId());

        // La baja y la reactivación tienen sus propios métodos: se conserva el estado activo actual
        pasajero.setActivo(actual.isActivo());
        pasajero.setTipoUsuario(TipoUsuario.PASAJERO);

        try {
            pasajeroDao.modificar(pasajero);
            pasajerosPorId.put(pasajero.getUsuarioId(), pasajero);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("modificar el pasajero", e);
        }
    }

    /** Realiza la baja lógica del pasajero en MySQL y actualiza su estado en memoria. */
    @Override
    public void baja(long id) {
        Pasajero pasajero = obtenerExistente(id);
        if (!pasajero.isActivo()) {
            throw new HotelException("El pasajero ya se encuentra dado de baja.");
        }

        try {
            pasajeroDao.baja(id);
            pasajero.setActivo(false);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("dar de baja al pasajero", e);
        }
    }

    /** Reactiva un pasajero dado de baja en MySQL y actualiza su estado en memoria. */
    @Override
    public void reactivar(long id) {
        Pasajero pasajero = obtenerExistente(id);
        if (pasajero.isActivo()) {
            throw new HotelException("El pasajero ya se encuentra activo.");
        }

        try {
            pasajeroDao.reactivar(id);
            pasajero.setActivo(true);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("reactivar al pasajero", e);
        }
    }

    /** Busca un pasajero por ID directamente en la colección en memoria. */
    @Override
    public Pasajero buscarPorId(long id) {
        return pasajerosPorId.get(id);
    }

    /** Busca un pasajero por su DNI en la colección en memoria, o null si no existe. */
    public Pasajero buscarPorDni(String dni) {
        if (esVacio(dni)) {
            return null;
        }
        String dniBuscado = dni.trim();
        for (Pasajero pasajero : pasajerosPorId.values()) {
            if (pasajero.getDni().equalsIgnoreCase(dniBuscado)) {
                return pasajero;
            }
        }
        return null;
    }

    /** Devuelve todos los pasajeros desde la colección en memoria. */
    @Override
    public List<Pasajero> listar() {
        return new ArrayList<>(pasajerosPorId.values());
    }

    /** Filtra y devuelve los pasajeros activos desde la colección en memoria. */
    @Override
    public List<Pasajero> listarActivos() {
        List<Pasajero> activos = new ArrayList<>();
        for (Pasajero pasajero : pasajerosPorId.values()) {
            if (pasajero.isActivo()) {
                activos.add(pasajero);
            }
        }
        return activos;
    }

    /** Filtra y devuelve los pasajeros dados de baja desde la colección en memoria. */
    @Override
    public List<Pasajero> listarInactivos() {
        List<Pasajero> inactivos = new ArrayList<>();
        for (Pasajero pasajero : pasajerosPorId.values()) {
            if (!pasajero.isActivo()) {
                inactivos.add(pasajero);
            }
        }
        return inactivos;
    }

    /** Genera el informe requerido de un pasajero (nombre, apellido, DNI, origen y domicilio). */
    public String generarInformePasajero(long id) {
        Pasajero p = obtenerExistente(id);
        return "Pasajero: " + p.getNombre() + " " + p.getApellido()
                + " | DNI: " + p.getDni()
                + " | Origen: " + p.getOrigen()
                + " | Domicilio: " + p.getDomicilioOrigen();
    }

    /** Carga inicial desde MySQL al Map en memoria. */
    private void cargarDesdeBase() {
        try {
            pasajerosPorId.clear();
            for (Pasajero pasajero : pasajeroDao.listar()) {
                pasajerosPorId.put(pasajero.getUsuarioId(), pasajero);
            }
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("cargar los pasajeros", e);
        }
    }

    /** Valida que los campos obligatorios del pasajero estén completos y sean coherentes. */
    private void validarPasajero(Pasajero pasajero) {
        if (pasajero == null) {
            throw new HotelException("Los datos del pasajero son obligatorios.");
        }
        if (esVacio(pasajero.getNombre()) || esVacio(pasajero.getApellido())) {
            throw new HotelException("El nombre y el apellido del pasajero son obligatorios.");
        }
        if (esVacio(pasajero.getDni())) {
            throw new HotelException("El DNI del pasajero es obligatorio.");
        }
        if (esVacio(pasajero.getEmail()) || !pasajero.getEmail().contains("@")) {
            throw new HotelException("Debe ingresar un email válido.");
        }
        if (esVacio(pasajero.getTelefono())) {
            throw new HotelException("El teléfono del pasajero es obligatorio.");
        }
        if (esVacio(pasajero.getNombreUsuario())) {
            throw new HotelException("El nombre de usuario es obligatorio.");
        }
        if (esVacio(pasajero.getContrasenia()) || pasajero.getContrasenia().length() < 4) {
            throw new HotelException("La contraseña debe tener al menos 4 caracteres.");
        }
        if (esVacio(pasajero.getOrigen()) || esVacio(pasajero.getDomicilioOrigen())) {
            throw new HotelException("El origen y el domicilio de origen son obligatorios.");
        }
    }

    /** Verifica en memoria que el DNI no pertenezca a otro pasajero. */
    private void verificarDniDisponible(String dni, long idActual) {
        for (Pasajero existente : pasajerosPorId.values()) {
            if (existente.getDni().equalsIgnoreCase(dni.trim()) && existente.getUsuarioId() != idActual) {
                throw new HotelException("Ya existe un pasajero registrado con el DNI " + dni + ".");
            }
        }
    }

    /** Verifica en memoria que el nombre de usuario no esté usado por otro pasajero. */
    private void verificarNombreUsuarioDisponible(String nombreUsuario, long idActual) {
        for (Pasajero existente : pasajerosPorId.values()) {
            if (existente.getNombreUsuario().equalsIgnoreCase(nombreUsuario.trim())
                    && existente.getUsuarioId() != idActual) {
                throw new HotelException("El nombre de usuario '" + nombreUsuario + "' ya está en uso.");
            }
        }
    }

    /** Obtiene un pasajero de la colección en memoria o lanza HotelException si no existe. */
    private Pasajero obtenerExistente(long id) {
        Pasajero pasajero = pasajerosPorId.get(id);
        if (pasajero == null) {
            throw new HotelException("No existe un pasajero con ID " + id + ".");
        }
        return pasajero;
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}