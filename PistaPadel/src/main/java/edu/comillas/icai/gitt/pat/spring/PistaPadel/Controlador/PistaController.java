package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Almacen.AlmacenMemoria;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/pistaPadel/courts")
public class PistaController {

    private static final Logger logger = LoggerFactory.getLogger(PistaController.class);

    private final AlmacenMemoria almacen;

    public PistaController(AlmacenMemoria almacen) {
        this.almacen = almacen;
    }

    // GET /pistaPadel/courts active=true/false
    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(name = "active", required = false) Boolean active) {

        List<Pista> res = new ArrayList<>(almacen.listarPistas());

        if (active != null) {
            res.removeIf(p -> p.isActiva() != active);
        }

        res.sort(Comparator.comparing(Pista::getIdPista));
        return ResponseEntity.ok(res);
    }

    // GET /pistaPadel/courts/{courtId}
    @GetMapping("/{courtId}")
    public ResponseEntity<?> detalle(@PathVariable int courtId) {

        Pista p = almacen.buscarPista(courtId);
        if (p == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }

        return ResponseEntity.ok(p);
    }

    // POST /pistaPadel/courts (ADMIN) -> 201 / 400 / 409
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@Valid @RequestBody ModeloPistaCrear req, BindingResult br) {

        if (br.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // No permitir negativos (null los permite)
        if (req.precioHora() < 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "precioHora inválido");
        }

        String nombreNorm = almacen.normalizarNombre(req.nombre());
        if (almacen.buscarPistaPorNombre(nombreNorm) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una pista con ese nombre");
        }

        Pista nueva = new Pista();
        nueva.setIdPista(almacen.generarIdPista());
        nueva.setNombre(req.nombre().trim());
        nueva.setUbicacion(req.ubicacion().trim());
        nueva.setPrecioHora(req.precioHora());
        nueva.setActiva(req.activa());
        nueva.setFechaAlta(req.fechaAlta());

        almacen.guardarPista(nueva);

        logger.info("Pista creada: id={}, nombre={}", nueva.getIdPista(), nueva.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    // PATCH /pistaPadel/courts/{courtId} (ADMIN) -> 200 / 400 / 404 / 409
    @PatchMapping("/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> modificar(@PathVariable int courtId,
                                       @RequestBody ModeloPistaPatch cambios) {

        Pista actual = almacen.buscarPista(courtId);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }
        if (cambios == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body vacío");
        }

        String oldNombre = actual.getNombre();
        boolean nombreCambiado = false;

        // nombre (si viene con) + 409 si hay pista (colisión)
        if (cambios.nombre() != null && !cambios.nombre().isBlank()) {
            String nuevoNombreNorm = almacen.normalizarNombre(cambios.nombre());

            Pista otra = almacen.buscarPistaPorNombre(nuevoNombreNorm);
            if (otra != null && otra.getIdPista() != courtId) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una pista con ese nombre");
            }

            actual.setNombre(cambios.nombre().trim());
            nombreCambiado = true;
        }

        // ubicacion (si viene)
        if (cambios.ubicacion() != null && !cambios.ubicacion().isBlank()) {
            actual.setUbicacion(cambios.ubicacion().trim());
        }

        // fechaAlta (si viene)
        if (cambios.fechaAlta() != null) {
            actual.setFechaAlta(cambios.fechaAlta());
        }

        // activa (si viene)
        if (cambios.activa() != null) {
            actual.setActiva(cambios.activa());
        }

        // precioHora (si viene)
        if (cambios.precioHora() != null) {
            if (cambios.precioHora() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "precioHora inválido");
            }
            actual.setPrecioHora(cambios.precioHora());
        }

        // Guardar manteniendo índice por nombre consistente
        if (nombreCambiado) {
            almacen.actualizarNombrePista(actual, oldNombre);
        } else {
            almacen.guardarPista(actual);
        }

        logger.info("Pista modificada: id={}", actual.getIdPista());
        return ResponseEntity.ok(actual);
    }

    // DELETE /pistaPadel/courts/{courtId} (ADMIN) -> 204 / 404
    @DeleteMapping("/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> borrar(@PathVariable int courtId) {

        boolean borrada = almacen.eliminarPista(courtId);
        if (!borrada) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }

        logger.info("Pista borrada: id={}", courtId);
        return ResponseEntity.noContent().build();
    }
}