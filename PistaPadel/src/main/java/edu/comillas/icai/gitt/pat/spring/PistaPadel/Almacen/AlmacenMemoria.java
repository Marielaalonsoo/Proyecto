package edu.comillas.icai.gitt.pat.spring.PistaPadel.Almacen;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.EstadoReserva;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Reserva;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Repository
public class AlmacenMemoria {

    private static final Logger logger = LoggerFactory.getLogger(AlmacenMemoria.class);

    private int nextUsuarioId = 1;
    private int nextPistaId = 1;
    private int nextReservaId = 1;

    private final Map<Integer, Usuario> usuariosPorId = new HashMap<>();
    private final Map<String, Integer> idUsuarioPorEmail = new HashMap<>();

    private final Map<Integer, Pista> pistasPorId = new HashMap<>();
    private final Map<String, Integer> idPistaPorNombre = new HashMap<>();

    private final Map<Integer, Reserva> reservasPorId = new HashMap<>();
    private final Map<Integer, List<Reserva>> reservasPorPista = new HashMap<>();

    // ----- IDs -----
    public int generarIdUsuario() { return nextUsuarioId++; }
    public int generarIdPista() { return nextPistaId++; }
    public int generarIdReserva() { return nextReservaId++; }

    // ----- Normalización -----
    public String normalizarEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    public String normalizarNombre(String nombre) {
        if (nombre == null) return null;
        return nombre.trim().toLowerCase();
    }

    // ----- Usuarios -----
    public Usuario buscarUsuarioPorId(Integer id) { return usuariosPorId.get(id); }

    public Usuario buscarUsuarioPorEmail(String email) {
        String key = normalizarEmail(email);
        Integer id = idUsuarioPorEmail.get(key);
        return (id == null) ? null : usuariosPorId.get(id);
    }

    public void guardarUsuario(Usuario usuario) {
        usuariosPorId.put(usuario.getIdUsuario(), usuario);
        idUsuarioPorEmail.put(normalizarEmail(usuario.getEmail()), usuario.getIdUsuario());
        logger.debug("Usuario guardado: id={}", usuario.getIdUsuario());
    }

    public Collection<Usuario> listarUsuarios() { return usuariosPorId.values(); }

    // ----- Pistas -----
    public Pista buscarPista(Integer id) { return pistasPorId.get(id); }

    public Pista buscarPistaPorNombre(String nombre) {
        String key = normalizarNombre(nombre);
        Integer id = idPistaPorNombre.get(key);
        return (id == null) ? null : pistasPorId.get(id);
    }

    public void guardarPista(Pista pista) {
        pistasPorId.put(pista.getIdPista(), pista);
        idPistaPorNombre.put(normalizarNombre(pista.getNombre()), pista.getIdPista());
        logger.debug("Pista guardada: id={}", pista.getIdPista());
    }

    public Collection<Pista> listarPistas() { return pistasPorId.values(); }

    public boolean eliminarPista(int idPista) {
        Pista p = pistasPorId.remove(idPista);
        if (p == null) return false;
        idPistaPorNombre.remove(normalizarNombre(p.getNombre()));
        return true;
    }

    // ----- Reservas -----
    public void guardarReserva(Reserva reserva) {
        reservasPorId.put(reserva.getIdReserva(), reserva);
        reservasPorPista.computeIfAbsent(reserva.getIdPista(), k -> new ArrayList<>()).add(reserva);
        logger.debug("Reserva guardada: id={}, pista={}", reserva.getIdReserva(), reserva.getIdPista());
    }

    public Reserva buscarReservaPorId(Integer id) { return reservasPorId.get(id); }

    public Collection<Reserva> listarReservas() { return reservasPorId.values(); }

    public void actualizarReserva(Reserva reserva, int oldPistaId) {
        reservasPorId.put(reserva.getIdReserva(), reserva);

        if (oldPistaId != reserva.getIdPista()) {
            List<Reserva> oldList = reservasPorPista.get(oldPistaId);
            if (oldList != null) {
                oldList.removeIf(r -> r.getIdReserva().equals(reserva.getIdReserva()));
            }
            reservasPorPista.computeIfAbsent(reserva.getIdPista(), k -> new ArrayList<>()).add(reserva);
        }

        logger.debug("Reserva actualizada: id={}, pista(old->new)={}->{}",
                reserva.getIdReserva(), oldPistaId, reserva.getIdPista());
    }

    public List<Reserva> reservasDePistaEnFecha(int idPista, LocalDate fecha) {
        List<Reserva> reservasDeEsaPista = reservasPorPista.get(idPista);
        if (reservasDeEsaPista == null) return List.of();

        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : reservasDeEsaPista) {
            if (r.getEstado() != EstadoReserva.ACTIVA) continue;
            if (!fecha.equals(r.getFechaReserva())) continue;
            resultado.add(r);
        }

        resultado.sort(Comparator.comparing(Reserva::getHoraInicio));
        return resultado;
    }

    // Solape real (no solo “misma hora exacta”)
    public boolean haySolape(int idPista, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        for (Reserva r : reservasDePistaEnFecha(idPista, fecha)) {
            boolean sePisan = inicio.isBefore(r.getHoraFin()) && fin.isAfter(r.getHoraInicio());
            if (sePisan) return true;
        }
        return false;
    }

    public boolean haySolapeExcluyendo(int idReservaExcluir, int idPista, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        for (Reserva r : reservasDePistaEnFecha(idPista, fecha)) {
            if (r.getIdReserva().equals(idReservaExcluir)) continue;
            boolean sePisan = inicio.isBefore(r.getHoraFin()) && fin.isAfter(r.getHoraInicio());
            if (sePisan) return true;
        }
        return false;
    }
}
