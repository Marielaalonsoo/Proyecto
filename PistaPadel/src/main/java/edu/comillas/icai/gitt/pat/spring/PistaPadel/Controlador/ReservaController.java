//package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;
//
//import edu.comillas.icai.gitt.pat.spring.PistaPadel.Almacen.AlmacenMemoria;
//import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/pistaPadel")
//public class ReservaController {
//
//   // @Autowired
//    //private AlmacenMemoria almacen;
//
//   private final AlmacenMemoria almacen = AlmacenMemoria.getAlmacen();
//
//    // Crea Reserva
//    @PostMapping("/reservations")
//    public ResponseEntity<Reserva> crearReserva(@RequestBody ModeloReserva modeloReserva, @RequestHeader(value = "Authorization", required = false) String token) {
//
//        Usuario usuario = null;
//        if (!almacen.usuarios.isEmpty()) {
//            usuario = almacen.usuarios.values().iterator().next(); // Coge el primer usuario que haya para probar
//        }
//
//        Pista pista = almacen.buscarPista(modeloReserva.courtId());
//        if (pista == null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista no existe");
//        }
//
//        Integer idReserva = ThreadLocalRandom.current().nextInt(10000, 99999);
//
//        // Crea reserva usando constructor
//        Reserva nuevaReserva = new Reserva(
//                idReserva,
//                (usuario != null) ? usuario.getIdUsuario() : 1, // ID usuario
//                modeloReserva.courtId(),
//                modeloReserva.date(),
//                modeloReserva.time(),
//                modeloReserva.durationMinutes(),
//                EstadoReserva.ACTIVA,
//                LocalDateTime.now()
//        );
//
//        almacen.guardarReserva(nuevaReserva);
//        return new ResponseEntity<>(nuevaReserva, HttpStatus.CREATED);
//    }
//
//    // Get disponibilidad
//    @GetMapping("/availability")
//    public ResponseEntity<String> checkAvailability(@RequestParam String date, @RequestParam(required = false) Integer courtId) {
//        // Implementación básica para cumplir el expediente: Devuelve OK siempre
//        return ResponseEntity.ok("Disponibilidad consultada para la fecha: " + date);
//    }
//
//    //Get disponibilidad (por pista)
//    @GetMapping("/courts/{courtId}/availability")
//    public ResponseEntity<String> checkCourtAvailability(@PathVariable Integer courtId, @RequestParam String date) {
//        if (almacen.buscarPista(courtId) == null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada");
//        }
//        return ResponseEntity.ok("Pista " + courtId + " disponible en " + date);
//    }
//}

package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Almacen.AlmacenMemoria;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/pistaPadel")
public class ReservaController {

    private final AlmacenMemoria almacen = AlmacenMemoria.getAlmacen();

    // POST /pistaPadel/reservations  -> 201 / 400 / 401 / 404 / 409
    @PostMapping("/reservations")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Reserva> crearReserva(@RequestBody ModeloReserva body) {

        // 400 básicos
        if (body == null || body.courtId() == null || body.date() == null || body.time() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body inválido");
        }
        if (body.durationMinutes() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "durationMinutes inválido");
        }

        Pista pista = almacen.buscarPista(body.courtId());
        if (pista == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista no existe");
        }

        // Regla: no se puede reservar una pista inactiva
        if (!pista.isActiva()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La pista esta inactiva");
        }

        LocalTime inicio = body.time();
        LocalTime fin = inicio.plusMinutes(body.durationMinutes());

        // Regla: no solapadas (409)
        if (almacen.haySolape(body.courtId(), body.date(), inicio, fin)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot ocupado");
        }

        // (Simplificación) usuario fijo para probar si aún no tienes auth integrada
        int idUsuario = 1;

        int idReserva = ThreadLocalRandom.current().nextInt(10000, 99999);

        Reserva nueva = new Reserva(
                idReserva,
                idUsuario,
                body.courtId(),
                body.date(),
                inicio,
                body.durationMinutes(),
                EstadoReserva.ACTIVA,
                LocalDateTime.now()
        );

        almacen.guardarReserva(nueva);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    // GET /pistaPadel/courts/{courtId}/availability?date=YYYY-MM-DD
    @GetMapping("/courts/{courtId}/availability")
    public ResponseEntity<List<Reserva>> disponibilidadPista(
            @PathVariable int courtId,
            @RequestParam String date
    ) {
        LocalDate d;
        try {
            d = LocalDate.parse(date);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date mal formato");
        }

        if (almacen.buscarPista(courtId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada");
        }

        // Devuelvo las reservas ACTIVAS de esa pista y fecha (simple y útil para probar)
        return ResponseEntity.ok(almacen.reservasDePistaEnFecha(courtId, d));
    }

    // GET /pistaPadel/availability?date=YYYY-MM-DD&courtId=...
    @GetMapping("/availability")
    public ResponseEntity<List<Reserva>> disponibilidadGeneral(
            @RequestParam String date,
            @RequestParam(required = false) Integer courtId
    ) {
        LocalDate d;
        try {
            d = LocalDate.parse(date);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date mal formato");
        }

        if (courtId == null) {
            // si no pasas courtId, devuelvo reservas de “todas las pistas” ese día (simple)
            // (en memoria: recorremos reservasPorId)
            return ResponseEntity.ok(
                    almacen.reservasPorId.values().stream()
                            .filter(r -> r.getEstado() == EstadoReserva.ACTIVA)
                            .filter(r -> d.equals(r.getFechaReserva()))
                            .toList()
            );
        } else {
            if (almacen.buscarPista(courtId) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada");
            }
            return ResponseEntity.ok(almacen.reservasDePistaEnFecha(courtId, d));
        }
    }
}
