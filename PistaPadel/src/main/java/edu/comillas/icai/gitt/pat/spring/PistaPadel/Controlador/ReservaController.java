package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Excepciones.ExcepcionDatosIncorrectos;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloReserva;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloReservaPatch;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Reserva;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.service.ReservaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/pistaPadel")
public class ReservaController {

    private static final Logger logger = LoggerFactory.getLogger(ReservaController.class);

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> crearReserva(@Valid @RequestBody ModeloReserva body,
                                          BindingResult br,
                                          Principal principal) {

        if (br.hasErrors()) throw new ExcepcionDatosIncorrectos(br);

        Reserva guardada = reservaService.crearReserva(body, principal);
        logger.info("Reserva creada: idReserva={}", guardada.getIdReserva());

        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @GetMapping("/reservations")
    public ResponseEntity<?> misReservas(@RequestParam(required = false) String from,
                                         @RequestParam(required = false) String to,
                                         Principal principal) {

        return ResponseEntity.ok(reservaService.obtenerMisReservas(from, to, principal));
    }

    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<?> obtenerReserva(@PathVariable Integer reservationId,
                                            Principal principal) {

        return ResponseEntity.ok(reservaService.obtenerReserva(reservationId, principal));
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<?> cancelarReserva(@PathVariable Integer reservationId,
                                             Principal principal) {

        reservaService.cancelarReserva(reservationId, principal);
        logger.info("Reserva cancelada: idReserva={}", reservationId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reservations/{reservationId}")
    public ResponseEntity<?> modificarReserva(@PathVariable Integer reservationId,
                                              @RequestBody ModeloReservaPatch body,
                                              Principal principal) {

        Reserva guardada = reservaService.modificarReserva(reservationId, body, principal);
        logger.info("Reserva modificada: idReserva={}", reservationId);

        return ResponseEntity.ok(guardada);
    }

    @GetMapping("/admin/reservations")
    public ResponseEntity<?> adminReservas(@RequestParam(required = false) String date,
                                           @RequestParam(required = false) Integer courtId,
                                           @RequestParam(required = false) Integer userId,
                                           Principal principal) {

        return ResponseEntity.ok(reservaService.obtenerReservasAdmin(date, courtId, userId, principal));
    }

    @GetMapping("/courts/{courtId}/availability")
    public ResponseEntity<?> disponibilidadPista(@PathVariable int courtId,
                                                 @RequestParam String date) {

        return ResponseEntity.ok(reservaService.obtenerDisponibilidadPista(courtId, date));
    }

    @GetMapping("/availability")
    public ResponseEntity<?> disponibilidadGeneral(@RequestParam String date,
                                                   @RequestParam(required = false) Integer courtId) {

        return ResponseEntity.ok(reservaService.obtenerDisponibilidadGeneral(date, courtId));
    }
}