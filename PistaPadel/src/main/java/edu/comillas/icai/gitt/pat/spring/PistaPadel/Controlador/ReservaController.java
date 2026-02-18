package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Almacen.AlmacenMemoria;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Excepciones.ExcepcionDatosIncorrectos;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.*;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/pistaPadel")
public class ReservaController {

    private static final Logger logger = LoggerFactory.getLogger(ReservaController.class);

    private final AlmacenMemoria almacen;

    public ReservaController(AlmacenMemoria almacen) {
        this.almacen = almacen;
    }

    private Usuario getUsuarioAutenticado(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        String username = principal.getName().trim();
        if (username.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");

        String emailNorm = almacen.normalizarEmail(username);

        Usuario u = almacen.buscarUsuarioPorEmail(emailNorm);
        if (u != null) {
            if (!u.isActivo()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario inactivo");
            return u;
        }

        Usuario nuevo = new Usuario();
        nuevo.setIdUsuario(almacen.generarIdUsuario());
        nuevo.setNombre(username);
        nuevo.setApellidos("");
        nuevo.setEmail(emailNorm);
        nuevo.setPassword("");
        nuevo.setTelefono("");
        nuevo.setRol("admin".equalsIgnoreCase(username) ? Rol.ADMIN : Rol.USER);
        nuevo.setFechaRegistro(LocalDateTime.now());
        nuevo.setActivo(true);

        almacen.guardarUsuario(nuevo);
        return nuevo;
    }

    // Nunca se llama a esAdmin(), solo !esAdmin() (no importa)
    private boolean esAdmin(Usuario u) {
        return u.getRol() == Rol.ADMIN;
    }

    private void exigirDuenoOAdmin(Usuario u, Reserva r) {
        boolean dueno = r.getIdUsuario().equals(u.getIdUsuario());
        if (!dueno && !esAdmin(u)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
    }

    private LocalDateTime parseFromTo(String s, boolean endOfDayIfDate) {
        if (s == null) return null;
        try {
            return LocalDateTime.parse(s);
        } catch (Exception ignored) { }
        try {
            LocalDate d = LocalDate.parse(s);
            return endOfDayIfDate ? d.atTime(23, 59, 59) : d.atStartOfDay();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from/to mal formato");
        }
    }

    private LocalDateTime inicioReserva(Reserva r) {
        return LocalDateTime.of(r.getFechaReserva(), r.getHoraInicio());
    }

    // POST /pistaPadel/reservations  -> 201 / 400 / 401 / 404 / 409
    @PostMapping("/reservations")
    public ResponseEntity<?> crearReserva(@Valid @RequestBody ModeloReserva body,
                                          BindingResult br,
                                          Principal principal) {

        if (br.hasErrors()) throw new ExcepcionDatosIncorrectos(br);

        Usuario u = getUsuarioAutenticado(principal);

        if (body.durationMinutes() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "durationMinutes inválido");
        }

        Pista pista = almacen.buscarPista(body.courtId());
        if (pista == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista no existe");
        if (!pista.isActiva()) throw new ResponseStatusException(HttpStatus.CONFLICT, "La pista está inactiva");

        LocalTime inicio = body.time();
        LocalTime fin = inicio.plusMinutes(body.durationMinutes());

        if (almacen.haySolape(body.courtId(), body.date(), inicio, fin)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }

        int idReserva = almacen.generarIdReserva();

        Reserva nueva = new Reserva(
                idReserva,
                u.getIdUsuario(),
                body.courtId(),
                body.date(),
                inicio,
                body.durationMinutes(),
                EstadoReserva.ACTIVA,
                LocalDateTime.now()
        );

        almacen.guardarReserva(nueva);
        logger.info("Reserva creada: idReserva={}, userId={}, courtId={}", idReserva, u.getIdUsuario(), body.courtId());

        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    // GET /pistaPadel/reservations Mis reservas  -> 200 / 401
    @GetMapping("/reservations")
    public ResponseEntity<?> misReservas(@RequestParam(required = false) String from,
                                         @RequestParam(required = false) String to,
                                         Principal principal) {

        Usuario u = getUsuarioAutenticado(principal);

        LocalDateTime fromDT = parseFromTo(from, false);
        LocalDateTime toDT = parseFromTo(to, true);

        List<Reserva> res = new ArrayList<>();
        for (Reserva r : almacen.listarReservas()) {
            if (!r.getIdUsuario().equals(u.getIdUsuario())) continue;

            LocalDateTime ini = inicioReserva(r);
            if (fromDT != null && ini.isBefore(fromDT)) continue;
            if (toDT != null && ini.isAfter(toDT)) continue;

            res.add(r);
        }

        res.sort(Comparator.comparing(Reserva::getFechaReserva).thenComparing(Reserva::getHoraInicio));
        return ResponseEntity.ok(res);
    }

    // GET /pistaPadel/reservations/{reservationId} -> 200 / 401 / 403 / 404
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<?> obtenerReserva(@PathVariable Integer reservationId,
                                            Principal principal) {

        Usuario u = getUsuarioAutenticado(principal);

        Reserva r = almacen.buscarReservaPorId(reservationId);
        if (r == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe");

        exigirDuenoOAdmin(u, r);
        return ResponseEntity.ok(r);
    }

    // DELETE /pistaPadel/reservations/{reservationId} -> 204 / 401 / 403 / 404 / 409
    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<?> cancelarReserva(@PathVariable Integer reservationId,
                                             Principal principal) {

        Usuario u = getUsuarioAutenticado(principal);

        Reserva r = almacen.buscarReservaPorId(reservationId);
        if (r == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe");

        exigirDuenoOAdmin(u, r);

        if (r.getEstado() == EstadoReserva.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya cancelada");
        }

        r.setEstado(EstadoReserva.CANCELADA);

        logger.info("Reserva cancelada: idReserva={}, porUserId={}", reservationId, u.getIdUsuario());
        return ResponseEntity.noContent().build();
    }

    // PATCH /pistaPadel/reservations/{reservationId} -> 200 / 400 / 401 / 403 / 404 / 409
    @PatchMapping("/reservations/{reservationId}")
    public ResponseEntity<?> modificarReserva(@PathVariable Integer reservationId,
                                              @RequestBody ModeloReservaPatch body,
                                              Principal principal) {

        Usuario u = getUsuarioAutenticado(principal);

        Reserva actual = almacen.buscarReservaPorId(reservationId);
        if (actual == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe");

        exigirDuenoOAdmin(u, actual);

        if (actual.getEstado() != EstadoReserva.ACTIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reserva no activa");
        }

        if (body == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body vacío");

        // Si viene null, mantener valor actual
        Integer newCourtId = (body.courtId() != null) ? body.courtId() : actual.getIdPista();
        LocalDate newDate = (body.date() != null) ? body.date() : actual.getFechaReserva();
        LocalTime newTime = (body.time() != null) ? body.time() : actual.getHoraInicio();
        Integer newDur = (body.durationMinutes() != null) ? body.durationMinutes() : actual.getDuracionMinutos();

        boolean cambiaAlgo =
                !Objects.equals(newCourtId, actual.getIdPista()) ||
                        !Objects.equals(newDate, actual.getFechaReserva()) ||
                        !Objects.equals(newTime, actual.getHoraInicio()) ||
                        !Objects.equals(newDur, actual.getDuracionMinutos());

        if (!cambiaAlgo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay cambios");
        }

        if (newDur <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "durationMinutes inválido");
        }

        Pista pista = almacen.buscarPista(newCourtId);
        if (pista == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista no existe");
        if (!pista.isActiva()) throw new ResponseStatusException(HttpStatus.CONFLICT, "La pista está inactiva");

        LocalTime newEnd = newTime.plusMinutes(newDur);

        // No solapamiento
        if (almacen.haySolapeExcluyendo(actual.getIdReserva(), newCourtId, newDate, newTime, newEnd)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }

        int oldPistaId = actual.getIdPista();

        Reserva updated = new Reserva(
                actual.getIdReserva(),
                actual.getIdUsuario(),
                newCourtId,
                newDate,
                newTime,
                newDur,
                actual.getEstado(),
                actual.getFechaCreacion()
        );

        almacen.actualizarReserva(updated, oldPistaId);

        logger.info("Reserva modificada: idReserva={}, porUserId={}", reservationId, u.getIdUsuario());
        return ResponseEntity.ok(updated);
    }

    // GET /pistaPadel/admin/reservations Ver reservas de todos -> 200 / 401 / 403
    @GetMapping("/admin/reservations")
    public ResponseEntity<?> adminReservas(@RequestParam(required = false) String date,
                                           @RequestParam(required = false) Integer courtId,
                                           @RequestParam(required = false) Integer userId,
                                           Principal principal) {

        Usuario u = getUsuarioAutenticado(principal);
        if (!esAdmin(u)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo admin");

        LocalDate d = null;
        if (date != null) {
            try { d = LocalDate.parse(date); }
            catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date mal formato"); }
        }

        List<Reserva> res = new ArrayList<>();
        for (Reserva r : almacen.listarReservas()) {
            if (d != null && !d.equals(r.getFechaReserva())) continue;
            if (courtId != null && !courtId.equals(r.getIdPista())) continue;
            if (userId != null && !userId.equals(r.getIdUsuario())) continue;
            res.add(r);
        }

        res.sort(Comparator.comparing(Reserva::getFechaReserva).thenComparing(Reserva::getHoraInicio));
        return ResponseEntity.ok(res);
    }

    // GET Availability
    @GetMapping("/courts/{courtId}/availability")
    public ResponseEntity<?> disponibilidadPista(@PathVariable int courtId,
                                                 @RequestParam String date) {

        LocalDate d;
        try { d = LocalDate.parse(date); }
        catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date mal formato"); }

        if (almacen.buscarPista(courtId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada");
        }

        return ResponseEntity.ok(almacen.reservasDePistaEnFecha(courtId, d));
    }

    @GetMapping("/availability")
    public ResponseEntity<?> disponibilidadGeneral(@RequestParam String date,
                                                   @RequestParam(required = false) Integer courtId) {

        LocalDate d;
        try { d = LocalDate.parse(date); }
        catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date mal formato"); }

        if (courtId != null) {
            if (almacen.buscarPista(courtId) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada");
            }
            return ResponseEntity.ok(almacen.reservasDePistaEnFecha(courtId, d));
        }

        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : almacen.listarReservas()) {
            if (r.getEstado() != EstadoReserva.ACTIVA) continue;
            if (!d.equals(r.getFechaReserva())) continue;
            resultado.add(r);
        }

        return ResponseEntity.ok(resultado);
    }
}
