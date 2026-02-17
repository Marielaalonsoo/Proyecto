package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Almacen.AlmacenMemoria;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Excepciones.ExcepcionDatosIncorrectos;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/pistaPadel")
public class ReservaController {

    private static final Logger logger = LoggerFactory.getLogger(ReservaController.class);

    private final AlmacenMemoria almacen;

    public ReservaController(AlmacenMemoria almacen) {
        this.almacen = almacen;
    }

    private Usuario getUsuarioLogueado(HttpSession session) {
        Object aux = session.getAttribute("idUsuario");
        if (!(aux instanceof Integer)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Integer id = (Integer) aux;
        Usuario u = almacen.buscarUsuarioPorId(id);

        if (u == null || !u.isActivo()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return u;
    }

    // POST /pistaPadel/reservations -> 201 / 400 / 401 / 404 / 409
    @PostMapping("/reservations")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> crearReserva(@Valid @RequestBody ModeloReserva body,
                                          BindingResult br,
                                          HttpSession session) {

        if (br.hasErrors()) {
            throw new ExcepcionDatosIncorrectos(br);
        }

        Usuario u = getUsuarioLogueado(session);

        if (body.durationMinutes() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "durationMinutes inválido");
        }

        Pista pista = almacen.buscarPista(body.courtId());
        if (pista == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista no existe");
        }

        if (!pista.isActiva()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La pista está inactiva");
        }

        LocalTime inicio = body.time();
        LocalTime fin = inicio.plusMinutes(body.durationMinutes());

        if (almacen.haySolape(body.courtId(), body.date(), inicio, fin)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }

        int idReserva = ThreadLocalRandom.current().nextInt(10000, 99999);

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

        logger.info("Reserva creada: idReserva={}, userId={}, courtId={}",
                idReserva, u.getIdUsuario(), body.courtId());

        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @GetMapping("/courts/{courtId}/availability")
    public ResponseEntity<?> disponibilidadPista(@PathVariable int courtId,
                                                 @RequestParam String date) {

        LocalDate d;
        try {
            d = LocalDate.parse(date);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date mal formato");
        }

        if (almacen.buscarPista(courtId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada");
        }

        return ResponseEntity.ok(almacen.reservasDePistaEnFecha(courtId, d));
    }

    @GetMapping("/availability")
    public ResponseEntity<?> disponibilidadGeneral(@RequestParam String date,
                                                   @RequestParam(required = false) Integer courtId) {

        LocalDate d;
        try {
            d = LocalDate.parse(date);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date mal formato");
        }

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
