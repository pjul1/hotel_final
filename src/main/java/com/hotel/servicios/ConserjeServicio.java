package com.hotel.servicios;

import com.hotel.dao.ConserjeDao;
import com.hotel.excepciones.HotelException;
import com.hotel.interfaces.IBajaLogica;
import com.hotel.interfaces.ICrud;
import com.hotel.interfaces.IListar;
import com.hotel.interfaces.IListarPorEstado;
import com.hotel.modelo.Conserje;
import com.hotel.modelo.enums.TipoUsuario;
import com.hotel.modelo.enums.Turno;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de negocio de los conserjes.
 * Implementa las mismas interfaces que ConserjeDao y mantiene una List en memoria
 * sincronizada con MySQL (Alternativa B) para resolver consultas y filtros sin
 * consultar continuamente la base de datos.
 */
public class ConserjeServicio implements ICrud<Conserje>, IBajaLogica, IListar<Conserje>, IListarPorEstado<Conserje> {

    private final ConserjeDao conserjeDao = new ConserjeDao();

    /** Colección en memoria que conserva el orden de los conserjes cargados. */
    private final List<Conserje> conserjes = new ArrayList<>();

    /** Carga inicial de los conserjes desde MySQL hacia la lista en memoria. */
    public ConserjeServicio() {
        cargarDesdeBase();
    }

    /**
     * Valida y registra un nuevo conserje en MySQL y, si tiene éxito,
     * lo agrega a la lista en memoria (Alternativa B).
     */
    @Override
    public void agregar(Conserje conserje) {
        validarConserje(conserje);
        verificarDniDisponible(conserje.getDni(), 0);
        verificarNombreUsuarioDisponible(conserje.getNombreUsuario(), 0);

        conserje.setActivo(true);
        conserje.setTipoUsuario(TipoUsuario.CONSERJE);

        try {
            conserjeDao.agregar(conserje);
            conserjes.add(conserje);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("registrar el conserje", e);
        }
    }

    /**
     * Valida y actualiza un conserje existente en MySQL y reemplaza su posición
     * en la lista en memoria.
     */
    @Override
    public void modificar(Conserje conserje) {
        validarConserje(conserje);
        Conserje actual = obtenerExistente(conserje.getUsuarioId());

        verificarDniDisponible(conserje.getDni(), conserje.getUsuarioId());
        verificarNombreUsuarioDisponible(conserje.getNombreUsuario(), conserje.getUsuarioId());

        // La baja y la reactivación tienen sus propios métodos: se conserva el estado actual
        conserje.setActivo(actual.isActivo());
        conserje.setTipoUsuario(TipoUsuario.CONSERJE);

        try {
            conserjeDao.modificar(conserje);
            int indice = conserjes.indexOf(actual);
            if (indice >= 0) {
                conserjes.set(indice, conserje);
            }
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("modificar el conserje", e);
        }
    }

    /** Realiza la baja lógica del conserje en MySQL y actualiza su estado en memoria. */
    @Override
    public void baja(long id) {
        Conserje conserje = obtenerExistente(id);
        if (!conserje.isActivo()) {
            throw new HotelException("El conserje ya se encuentra dado de baja.");
        }

        try {
            conserjeDao.baja(id);
            conserje.setActivo(false);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("dar de baja al conserje", e);
        }
    }

    /** Reactiva un conserje dado de baja en MySQL y actualiza su estado en memoria. */
    @Override
    public void reactivar(long id) {
        Conserje conserje = obtenerExistente(id);
        if (conserje.isActivo()) {
            throw new HotelException("El conserje ya se encuentra activo.");
        }

        try {
            conserjeDao.reactivar(id);
            conserje.setActivo(true);
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("reactivar al conserje", e);
        }
    }

    /** Busca un conserje por ID en la lista en memoria, o devuelve null si no existe. */
    @Override
    public Conserje buscarPorId(long id) {
        for (Conserje conserje : conserjes) {
            if (conserje.getUsuarioId() == id) {
                return conserje;
            }
        }
        return null;
    }

    /** Devuelve una copia de la lista completa de conserjes en memoria. */
    @Override
    public List<Conserje> listar() {
        return new ArrayList<>(conserjes);
    }

    /** Filtra y devuelve los conserjes activos desde la lista en memoria. */
    @Override
    public List<Conserje> listarActivos() {
        List<Conserje> activos = new ArrayList<>();
        for (Conserje conserje : conserjes) {
            if (conserje.isActivo()) {
                activos.add(conserje);
            }
        }
        return activos;
    }

    /** Filtra y devuelve los conserjes dados de baja desde la lista en memoria. */
    @Override
    public List<Conserje> listarInactivos() {
        List<Conserje> inactivos = new ArrayList<>();
        for (Conserje conserje : conserjes) {
            if (!conserje.isActivo()) {
                inactivos.add(conserje);
            }
        }
        return inactivos;
    }

    /** Filtra y devuelve los conserjes activos que trabajan en un turno específico. */
    public List<Conserje> listarPorTurno(Turno turno) {
        if (turno == null) {
            throw new HotelException("Debe indicar el turno a consultar.");
        }
        List<Conserje> porTurno = new ArrayList<>();
        for (Conserje conserje : conserjes) {
            if (conserje.isActivo() && conserje.getTurno() == turno) {
                porTurno.add(conserje);
            }
        }
        return porTurno;
    }

    /** Carga inicial desde MySQL a la lista en memoria. */
    private void cargarDesdeBase() {
        try {
            conserjes.clear();
            conserjes.addAll(conserjeDao.listar());
        } catch (SQLException e) {
            throw ValidacionesServicio.errorDeBase("cargar los conserjes", e);
        }
    }

    /** Valida que los campos personales y laborales del conserje estén completos y sean válidos. */
    private void validarConserje(Conserje conserje) {
        if (conserje == null) {
            throw new HotelException("Los datos del conserje son obligatorios.");
        }
        if (esVacio(conserje.getNombre()) || esVacio(conserje.getApellido())) {
            throw new HotelException("El nombre y el apellido del conserje son obligatorios.");
        }
        if (esVacio(conserje.getDni())) {
            throw new HotelException("El DNI del conserje es obligatorio.");
        }
        if (esVacio(conserje.getEmail()) || !conserje.getEmail().contains("@")) {
            throw new HotelException("Debe ingresar un email válido.");
        }
        if (esVacio(conserje.getTelefono())) {
            throw new HotelException("El teléfono del conserje es obligatorio.");
        }
        if (esVacio(conserje.getNombreUsuario())) {
            throw new HotelException("El nombre de usuario es obligatorio.");
        }
        if (esVacio(conserje.getContrasenia()) || conserje.getContrasenia().length() < 4) {
            throw new HotelException("La contraseña debe tener al menos 4 caracteres.");
        }
        if (conserje.getSalario() <= 0) {
            throw new HotelException("El salario debe ser mayor a 0.");
        }
        if (conserje.getTurno() == null) {
            throw new HotelException("Debe seleccionar el turno del conserje.");
        }
    }

    /** Verifica en memoria que el DNI no pertenezca a otro conserje. */
    private void verificarDniDisponible(String dni, long idActual) {
        for (Conserje existente : conserjes) {
            if (existente.getDni().equalsIgnoreCase(dni.trim()) && existente.getUsuarioId() != idActual) {
                throw new HotelException("Ya existe un conserje registrado con el DNI " + dni + ".");
            }
        }
    }

    /** Verifica en memoria que el nombre de usuario no esté usado por otro conserje. */
    private void verificarNombreUsuarioDisponible(String nombreUsuario, long idActual) {
        for (Conserje existente : conserjes) {
            if (existente.getNombreUsuario().equalsIgnoreCase(nombreUsuario.trim())
                    && existente.getUsuarioId() != idActual) {
                throw new HotelException("El nombre de usuario '" + nombreUsuario + "' ya está en uso.");
            }
        }
    }

    /** Obtiene un conserje de la lista en memoria o lanza HotelException si no existe. */
    private Conserje obtenerExistente(long id) {
        Conserje conserje = buscarPorId(id);
        if (conserje == null) {
            throw new HotelException("No existe un conserje con ID " + id + ".");
        }
        return conserje;
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}