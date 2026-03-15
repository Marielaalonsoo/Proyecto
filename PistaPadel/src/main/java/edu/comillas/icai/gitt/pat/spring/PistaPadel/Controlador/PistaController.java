package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

//import edu.comillas.icai.gitt.pat.spring.PistaPadel.Almacen.AlmacenMemoria;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloPistaCrear;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloPistaPatch;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Pista;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

//import java.util.ArrayList;
//import java.util.Comparator;
import java.time.LocalDate;
import java.util.List;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio.RepoPista;

@RestController
@RequestMapping("/pistaPadel/courts")
public class PistaController {

    private static final Logger logger = LoggerFactory.getLogger(PistaController.class);

    //private final AlmacenMemoria almacen;
    private final RepoPista repoPista;

    //public PistaController(AlmacenMemoria almacen) {
        //this.almacen = almacen;
    //}
    public PistaController(RepoPista repoPista) {
        this.repoPista = repoPista;
    }

    // GET /pistaPadel/courts active=true/false
    // GET /pistaPadel/courts active=true/false
    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(name = "active", required = false) Boolean active) {

        List<Pista> res;

        if (active != null) {
            res = repoPista.findByActiva(active);
        } else {
            res = repoPista.findAll();
        }

        res.sort(java.util.Comparator.comparing(Pista::getIdPista));
        return ResponseEntity.ok(res);
    }

    // GET /pistaPadel/courts/{courtId}
    @GetMapping("/{courtId}")
    public ResponseEntity<?> detalle(@PathVariable int courtId) {

        Pista p = repoPista.findById(courtId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe"));

        return ResponseEntity.ok(p);
    }

    // POST /pistaPadel/courts (ADMIN) -> 201 / 400 / 409
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@Valid @RequestBody ModeloPistaCrear req, BindingResult br) {

        if (br.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (req.precioHora() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "precioHora inválido");
        }

        String nombre = req.nombre().trim();

        if (repoPista.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una pista con ese nombre");
        }

        Pista nueva = new Pista();
        nueva.setIdPista(null);
        nueva.setNombre(nombre);
        nueva.setUbicacion(req.ubicacion().trim());
        nueva.setPrecioHora(req.precioHora());
        nueva.setActiva(req.activa());
        nueva.setFechaAlta(req.fechaAlta() != null ? req.fechaAlta() : LocalDate.now());

        Pista guardada = repoPista.save(nueva);

        logger.info("Pista creada: id={}, nombre={}", guardada.getIdPista(), guardada.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    // PATCH /pistaPadel/courts/{courtId} (ADMIN) -> 200 / 400 / 404 / 409
    @PatchMapping("/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> modificar(@PathVariable int courtId,
                                       @RequestBody ModeloPistaPatch cambios) {

        Pista actual = repoPista.findById(courtId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe"));

        if (cambios == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body vacío");
        }

        if (cambios.nombre() != null && !cambios.nombre().isBlank()) {
            String nuevoNombre = cambios.nombre().trim();

            Pista otra = repoPista.findByNombreIgnoreCase(nuevoNombre).orElse(null);
            if (otra != null && !otra.getIdPista().equals(courtId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una pista con ese nombre");
            }

            actual.setNombre(nuevoNombre);
        }

        if (cambios.ubicacion() != null && !cambios.ubicacion().isBlank()) {
            actual.setUbicacion(cambios.ubicacion().trim());
        }

        if (cambios.fechaAlta() != null) {
            actual.setFechaAlta(cambios.fechaAlta());
        }

        if (cambios.activa() != null) {
            actual.setActiva(cambios.activa());
        }

        if (cambios.precioHora() != null) {
            if (cambios.precioHora() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "precioHora inválido");
            }
            actual.setPrecioHora(cambios.precioHora());
        }

        Pista guardada = repoPista.save(actual);

        logger.info("Pista modificada: id={}", guardada.getIdPista());
        return ResponseEntity.ok(guardada);
    }

    // DELETE /pistaPadel/courts/{courtId} (ADMIN) -> 204 / 404
    @DeleteMapping("/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> borrar(@PathVariable int courtId) {

        if (!repoPista.existsById(courtId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }

        repoPista.deleteById(courtId);

        logger.info("Pista borrada: id={}", courtId);
        return ResponseEntity.noContent().build();
    }
}