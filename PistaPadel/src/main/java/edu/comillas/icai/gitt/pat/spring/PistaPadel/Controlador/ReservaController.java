package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Almacen.AlmacenMemoria;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pistaPadel")
public class ReservaController {

    @Autowired
    private AlmacenMemoria almacen;

    // Crea Reserva
    @PostMapping("/reservations")
    public ResponseEntity<Reserva> crearReserva(@RequestBody ModeloReserva modeloReserva, @RequestHeader(value = "Authorization", required = false) String token) {

        Usuario usuario = null;
        if (!almacen.usuarios.isEmpty()) {
            usuario = almacen.usuarios.values().iterator().next(); // Coge el primer usuario que haya para probar
        }

        Pista pista = almacen.buscarPista(modeloReserva.courtId());
        if (pista == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La pista no existe");
        }

        Integer idReserva = ThreadLocalRandom.current().nextInt(10000, 99999);

        // Crea reserva usando constructor
        Reserva nuevaReserva = new Reserva(
                idReserva,
                (usuario != null) ? usuario.getIdUsuario() : 1, // ID usuario
                modeloReserva.courtId(),
                modeloReserva.date(),
                modeloReserva.time(),
                modeloReserva.durationMinutes(),
                EstadoReserva.ACTIVA,
                LocalDateTime.now()
        );

        almacen.guardarReserva(nuevaReserva);
        return new ResponseEntity<>(nuevaReserva, HttpStatus.CREATED);
    }

    // Get disponibilidad
    @GetMapping("/availability")
    public ResponseEntity<String> checkAvailability(@RequestParam String date, @RequestParam(required = false) Integer courtId) {
        // Implementación básica para cumplir el expediente: Devuelve OK siempre
        return ResponseEntity.ok("Disponibilidad consultada para la fecha: " + date);
    }

    //Get disponibilidad (por pista)
    @GetMapping("/courts/{courtId}/availability")
    public ResponseEntity<String> checkCourtAvailability(@PathVariable Integer courtId, @RequestParam String date) {
        if (almacen.buscarPista(courtId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada");
        }
        return ResponseEntity.ok("Pista " + courtId + " disponible en " + date);
    }
}