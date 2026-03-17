package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Excepciones.ExcepcionDatosIncorrectos;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloPistaCrear;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloPistaPatch;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.service.PistaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pistaPadel/courts")
public class PistaController {

    private static final Logger logger = LoggerFactory.getLogger(PistaController.class);

    private final PistaService pistaService;

    public PistaController(PistaService pistaService) {
        this.pistaService = pistaService;
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(name = "active", required = false) Boolean active) {
        return ResponseEntity.ok(pistaService.listar(active));
    }

    @GetMapping("/{courtId}")
    public ResponseEntity<?> detalle(@PathVariable int courtId) {
        return ResponseEntity.ok(pistaService.detalle(courtId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@Valid @RequestBody ModeloPistaCrear req, BindingResult br) {
        if (br.hasErrors()) throw new ExcepcionDatosIncorrectos(br);

        Pista guardada = pistaService.crear(req);
        logger.info("Pista creada: id={}, nombre={}", guardada.getIdPista(), guardada.getNombre());

        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @PatchMapping("/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> modificar(@PathVariable int courtId,
                                       @RequestBody ModeloPistaPatch cambios) {
        Pista guardada = pistaService.modificar(courtId, cambios);
        logger.info("Pista modificada: id={}", guardada.getIdPista());

        return ResponseEntity.ok(guardada);
    }

    @DeleteMapping("/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> borrar(@PathVariable int courtId) {
        pistaService.desactivarOBorrar(courtId);
        logger.info("Pista desactivada: id={}", courtId);

        return ResponseEntity.noContent().build();
    }
}