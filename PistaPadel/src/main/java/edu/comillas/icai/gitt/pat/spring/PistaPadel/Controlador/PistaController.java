package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Almacen.AlmacenMemoria;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Pista;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/pistaPadel/courts")
public class PistaController {

    private final AlmacenMemoria almacen = AlmacenMemoria.getAlmacen();

    // GET /pistaPadel/courts?active=true|false
    @GetMapping("/pistaPadel/courts")
    public List<Pista> listar(@RequestParam(name = "active", required = false) Boolean active) {
        List<Pista> res = new ArrayList<>(almacen.pistasPorId.values());
        if (active != null) {
            res.removeIf(p -> p.isActiva() != active);
        }
        res.sort(Comparator.comparing(Pista::getIdPista));
        return res;
    }

    // GET /pistaPadel/courts/{courtId}
    @GetMapping("/pistaPadel/courts/{courtId}")
    public Pista detalle(@PathVariable int courtId) {
        Pista p = almacen.pistasPorId.get(courtId);
        if (p == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }
        return p;
    }

    // POST /pistaPadel/courts  (ADMIN)  -> 201 / 400 / 409
    @PostMapping("/pistaPadel/courts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Pista crear(@RequestBody Pista nueva) {

        // 400: body inválido o faltan campos (en POST tú dijiste: "hay que meter todos")
        if (nueva == null
                || nueva.getNombre() == null || nueva.getNombre().isBlank()
                || nueva.getUbicacion() == null || nueva.getUbicacion().isBlank()
                || nueva.getFechaAta() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos inválidos");
        }

        // normalizar + comprobar duplicado por nombre (409)
        String nombreNorm = almacen.normalizarNombre(nueva.getNombre());
        Integer idExistente = almacen.idPistaPorNombre.get(nombreNorm);
        if (idExistente != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una pista con ese nombre");
        }

        // generar ID y guardar en ambos mapas
        int id = almacen.generarIdPista();
        nueva.setIdPista(id);

        almacen.pistasPorId.put(id, nueva);
        almacen.idPistaPorNombre.put(nombreNorm, id);

        return nueva;
    }

    // PATCH /pistaPadel/courts/{courtId} (ADMIN) -> 200 / 400 / 404 / 409
    @PatchMapping("/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Pista modificar(@PathVariable int courtId, @RequestBody Pista cambios) {

        Pista actual = almacen.pistasPorId.get(courtId);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }
        if (cambios == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body vacío");
        }

        // nombre (si viene) + 409 si colisiona
        if (cambios.getNombre() != null && !cambios.getNombre().isBlank()) {
            String nuevoNombreNorm = almacen.normalizarNombre(cambios.getNombre());

            Integer otroId = almacen.idPistaPorNombre.get(nuevoNombreNorm);
            if (otroId != null && otroId != courtId) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una pista con ese nombre");
            }

            // actualizar índice nombre -> id
            String viejoNombreNorm = almacen.normalizarNombre(actual.getNombre());
            almacen.idPistaPorNombre.remove(viejoNombreNorm);

            actual.setNombre(cambios.getNombre());
            almacen.idPistaPorNombre.put(nuevoNombreNorm, courtId);
        }

        // ubicacion (si viene)
        if (cambios.getUbicacion() != null && !cambios.getUbicacion().isBlank()) {
            actual.setUbicacion(cambios.getUbicacion());
        }

        // fecha (si viene)
        if (cambios.getFechaAta() != null) {
            actual.setFechaAta(cambios.getFechaAta());
        }

        // activa: al ser boolean, aquí lo aplicamos siempre (simple)
        actual.setActiva(cambios.isActiva());

        // precioHora: al ser int, aquí lo aplicamos siempre (simple)
        // Si quieres impedir negativos:
        if (cambios.getPrecioHora() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "precioHora inválido");
        }
        actual.setPrecioHora(cambios.getPrecioHora());

        almacen.pistasPorId.put(courtId, actual);
        return actual;
    }

    // DELETE /pistaPadel/courts/{courtId} (ADMIN) -> 204 / 404
    @DeleteMapping("/{courtId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void borrar(@PathVariable int courtId) {
        Pista p = almacen.pistasPorId.remove(courtId);
        if (p == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe");
        }
        String nombreNorm = almacen.normalizarNombre(p.getNombre());
        almacen.idPistaPorNombre.remove(nombreNorm);
    }
}
