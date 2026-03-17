package edu.comillas.icai.gitt.pat.spring.PistaPadel.service;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.*;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio.RepoReserva;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio.RepoUsuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ReservaService {

    private final RepoReserva repoReserva;
    private final RepoUsuario repoUsuario;
    private final RepoPista repoPista;

    public ReservaService(RepoReserva repoReserva, RepoUsuario repoUsuario, RepoPista repoPista) {
        this.repoReserva = repoReserva;
        this.repoUsuario = repoUsuario;
        this.repoPista = repoPista;
    }

    public Usuario obtenerUsuarioAutenticado(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        String email = principal.getName().trim();
        if (email.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        return repoUsuario.findByEmailIgnoreCase(email)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido o inactivo"));
    }

    public Reserva crearReserva(ModeloReserva body, Principal principal) {
        Usuario usuario = obtenerUsuarioAutenticado(principal);

        if (body.durationMinutes() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "durationMinutes inválido");
        }

        Pista pista = repoPista.findById(body.courtId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista no existe"));

        if (!pista.isActiva()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La pista está inactiva");
        }

        LocalTime inicio = body.time();
        LocalTime fin = inicio.plusMinutes(body.durationMinutes());

        if (haySolape(body.courtId(), body.date(), inicio, fin)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }

        Reserva nueva = new Reserva(
                null,
                usuario,
                pista,
                body.date(),
                inicio,
                body.durationMinutes(),
                EstadoReserva.ACTIVA,
                LocalDateTime.now()
        );

        return repoReserva.save(nueva);
    }

    public List<Reserva> obtenerMisReservas(String from, String to, Principal principal) {
        Usuario usuario = obtenerUsuarioAutenticado(principal);

        LocalDateTime fromDT = parseFromTo(from, false);
        LocalDateTime toDT = parseFromTo(to, true);

        List<Reserva> reservas = new ArrayList<>(
                repoReserva.findByUsuario_IdUsuarioOrderByFechaReservaAscHoraInicioAsc(usuario.getIdUsuario())
        );

        if (fromDT != null || toDT != null) {
            reservas.removeIf(r -> {
                LocalDateTime ini = LocalDateTime.of(r.getFechaReserva(), r.getHoraInicio());
                if (fromDT != null && ini.isBefore(fromDT)) return true;
                if (toDT != null && ini.isAfter(toDT)) return true;
                return false;
            });
        }

        return reservas;
    }

    public Reserva obtenerReserva(Integer reservationId, Principal principal) {
        Usuario usuario = obtenerUsuarioAutenticado(principal);

        Reserva reserva = repoReserva.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe"));

        exigirDuenoOAdmin(usuario, reserva);
        return reserva;
    }

    public void cancelarReserva(Integer reservationId, Principal principal) {
        Usuario usuario = obtenerUsuarioAutenticado(principal);

        Reserva reserva = repoReserva.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe"));

        exigirDuenoOAdmin(usuario, reserva);

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya cancelada");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        repoReserva.save(reserva);
    }

    public Reserva modificarReserva(Integer reservationId, ModeloReservaPatch body, Principal principal) {
        Usuario usuario = obtenerUsuarioAutenticado(principal);

        Reserva actual = repoReserva.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe"));

        exigirDuenoOAdmin(usuario, actual);

        if (actual.getEstado() != EstadoReserva.ACTIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reserva no activa");
        }

        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body vacío");
        }

        Integer newCourtId = (body.courtId() != null) ? body.courtId() : actual.getPista().getIdPista();
        LocalDate newDate = (body.date() != null) ? body.date() : actual.getFechaReserva();
        LocalTime newTime = (body.time() != null) ? body.time() : actual.getHoraInicio();
        Integer newDur = (body.durationMinutes() != null) ? body.durationMinutes() : actual.getDuracionMinutos();

        boolean cambiaAlgo =
                !Objects.equals(newCourtId, actual.getPista().getIdPista()) ||
                        !Objects.equals(newDate, actual.getFechaReserva()) ||
                        !Objects.equals(newTime, actual.getHoraInicio()) ||
                        !Objects.equals(newDur, actual.getDuracionMinutos());

        if (!cambiaAlgo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay cambios");
        }

        if (newDur <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "durationMinutes inválido");
        }

        Pista pista = repoPista.findById(newCourtId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista no existe"));

        if (!pista.isActiva()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La pista está inactiva");
        }

        LocalTime newEnd = newTime.plusMinutes(newDur);

        if (haySolapeExcluyendo(actual.getIdReserva(), newCourtId, newDate, newTime, newEnd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }

        actual.setPista(pista);
        actual.setFechaReserva(newDate);
        actual.setHoraInicio(newTime);
        actual.setDuracionMinutos(newDur);

        return repoReserva.save(actual);
    }

    public List<Reserva> obtenerReservasAdmin(String date, Integer courtId, Integer userId, Principal principal) {
        Usuario usuario = obtenerUsuarioAutenticado(principal);
        if (!esAdmin(usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo admin");
        }

        LocalDate fecha = null;
        if (date != null) {
            try {
                fecha = LocalDate.parse(date);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date mal formato");
            }
        }

        List<Reserva> reservas = new ArrayList<>();
        repoReserva.findAll().forEach(reservas::add);

        LocalDate finalFecha = fecha;
        reservas.removeIf(r ->
                (finalFecha != null && !finalFecha.equals(r.getFechaReserva())) ||
                        (courtId != null && !courtId.equals(r.getPista().getIdPista())) ||
                        (userId != null && !userId.equals(r.getUsuario().getIdUsuario()))
        );

        reservas.sort(
                Comparator.comparing(Reserva::getFechaReserva)
                        .thenComparing(Reserva::getHoraInicio)
        );

        return reservas;
    }

    public List<Reserva> obtenerDisponibilidadPista(int courtId, String date) {
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(date);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date mal formato");
        }

        if (!repoPista.existsById(courtId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada");
        }

        return reservasActivasDePistaEnFecha(courtId, fecha);
    }

    public List<Reserva> obtenerDisponibilidadGeneral(String date, Integer courtId) {
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(date);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date mal formato");
        }

        if (courtId != null) {
            if (!repoPista.existsById(courtId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada");
            }
            return reservasActivasDePistaEnFecha(courtId, fecha);
        }

        List<Reserva> resultado = new ArrayList<>();
        repoReserva.findAll().forEach(resultado::add);

        resultado.removeIf(r -> r.getEstado() != EstadoReserva.ACTIVA || !fecha.equals(r.getFechaReserva()));
        resultado.sort(
                Comparator.comparing(
                        Reserva::getPista,
                        Comparator.comparing(Pista::getIdPista)
                ).thenComparing(Reserva::getHoraInicio)
        );

        return resultado;
    }

    private boolean esAdmin(Usuario usuario) {
        return usuario.getRol() == Rol.ADMIN;
    }

    private void exigirDuenoOAdmin(Usuario usuario, Reserva reserva) {
        boolean dueno = reserva.getUsuario().getIdUsuario().equals(usuario.getIdUsuario());
        if (!dueno && !esAdmin(usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }
    }

    private LocalDateTime parseFromTo(String s, boolean endOfDayIfDate) {
        if (s == null) return null;

        try {
            return LocalDateTime.parse(s);
        } catch (Exception ignored) {
        }

        try {
            LocalDate d = LocalDate.parse(s);
            return endOfDayIfDate ? d.atTime(23, 59, 59) : d.atStartOfDay();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from/to mal formato");
        }
    }

    private List<Reserva> reservasActivasDePistaEnFecha(Integer courtId, LocalDate date) {
        return repoReserva.findByPista_IdPistaAndFechaReservaAndEstadoOrderByHoraInicioAsc(
                courtId, date, EstadoReserva.ACTIVA
        );
    }

    private boolean haySolape(Integer courtId, LocalDate date, LocalTime inicio, LocalTime fin) {
        List<Reserva> reservas = reservasActivasDePistaEnFecha(courtId, date);

        for (Reserva r : reservas) {
            LocalTime existenteInicio = r.getHoraInicio();
            LocalTime existenteFin = r.getHoraFin();

            boolean solapa = inicio.isBefore(existenteFin) && fin.isAfter(existenteInicio);
            if (solapa) {
                return true;
            }
        }
        return false;
    }

    private boolean haySolapeExcluyendo(Integer reservaId, Integer courtId, LocalDate date, LocalTime inicio, LocalTime fin) {
        List<Reserva> reservas = reservasActivasDePistaEnFecha(courtId, date);

        for (Reserva r : reservas) {
            if (r.getIdReserva().equals(reservaId)) {
                continue;
            }

            LocalTime existenteInicio = r.getHoraInicio();
            LocalTime existenteFin = r.getHoraFin();

            boolean solapa = inicio.isBefore(existenteFin) && fin.isAfter(existenteInicio);
            if (solapa) {
                return true;
            }
        }
        return false;
    }
}