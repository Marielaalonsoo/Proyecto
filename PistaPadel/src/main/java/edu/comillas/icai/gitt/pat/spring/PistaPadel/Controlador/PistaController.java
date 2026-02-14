package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Almacen.AlmacenMemoria;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloCampoIncorrecto;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloPista;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Excepciones.ExcepcionPistaIncorrecta;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class PistaController {
    private final AlmacenMemoria almacen = AlmacenMemoria.getAlmacen();

    @PostMapping("/pistaPadel/courts")
    @ResponseStatus(HttpStatus.CREATED)
    public Pista crea(@Valid @RequestBody ModeloPista pistaNueva, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw
                    new ExcepcionPistaIncorrecta(bindingResult); // 400
        }

        String nombre = almacen.normalizarNombre(pistaNueva.nombre());
        if (almacen.idPistaPorNombre.containsKey(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nombre de pista duplicado");
        }
        Integer id = almacen.generarIdPista(); //generar un idpista
        Pista pista = new Pista(id,
                pistaNueva.nombre(),
                pistaNueva.ubicacion(),
                true,              // siempre activa al crear
                LocalDate.now());


        almacen.pistasPorId.put(id, pista);
        almacen.idPistaPorNombre.put(nombre, id);

        return pista;
    }
// ========= GET /pistaPadel/courts?active=true/false =========
        @GetMapping("/pistaPadel/courts")
        @ResponseStatus(HttpStatus.OK) // 200
        public List<Pista> lista(@RequestParam(required = false) Boolean active) {

            if (active == null) return new ArrayList<>(almacen.pistasPorId.values());

            return almacen.pistasPorId.values().stream()
                    .filter(p -> p.isActiva() == active)
                    .collect(Collectors.toList());
        }

        // ========= GET /pistaPadel/courts/{courtId} =========
        @GetMapping("/pistaPadel/courts/{courtId}")
        @ResponseStatus(HttpStatus.OK) // 200
        public Pista lee(@PathVariable Integer courtId) {

            Pista pista = almacen.pistasPorId.get(courtId);

            if (pista == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la pista"); // 404
            }

            return pista;
        }

        // ========= PATCH /pistaPadel/courts/{courtId} (ADMIN) =========
        @PatchMapping("/pistaPadel/courts/{courtId}")
        @ResponseStatus(HttpStatus.OK) // 200
        public Pista modifica(@PathVariable Integer courtId,
                @Valid @RequestBody ModeloPista cambios,
                BindingResult bindingResult) {

            if (bindingResult.hasErrors()) {
                throw new ExcepcionPistaIncorrecta(bindingResult); // 400
            }

            Pista existente = almacen.pistasPorId.get(courtId);
            if (existente == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la pista"); // 404
            }

            // nombre único si cambia
            if (cambios.nombre() != null && !cambios.nombre().equalsIgnoreCase(existente.getNombre())) {

                String nuevoNorm = almacen.normalizarNombre(cambios.nombre());
                Integer idExistenteConEseNombre = almacen.idPistaPorNombre.get(nuevoNorm);

                // si existe y NO es esta misma pista -> conflicto
                if (idExistenteConEseNombre != null && !idExistenteConEseNombre.equals(courtId)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Nombre de pista duplicado"); // 409
                }

                // actualizar índice nombre->id (quita el viejo y mete el nuevo)
                String viejoNorm = almacen.normalizarNombre(existente.getNombre());
                almacen.idPistaPorNombre.remove(viejoNorm);

                existente.setNombre(cambios.nombre());
                almacen.idPistaPorNombre.put(nuevoNorm, courtId);
            }

            if (cambios.ubicacion() != null) {
                existente.setUbicacion(cambios.ubicacion());
            }

            if (cambios.activa() != null) {
                existente.setActiva(cambios.activa());
            }

            almacen.pistasPorId.put(courtId, existente);
            return existente;
        }

        // ========= DELETE /pistaPadel/courts/{courtId} (ADMIN) =========
        @DeleteMapping("/pistaPadel/courts/{courtId}")
        @ResponseStatus(HttpStatus.NO_CONTENT) // 204
        public void borrar(@PathVariable Integer courtId) {

            Pista pista = almacen.pistasPorId.get(courtId);
            if (pista == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la pista a borrar"); // 404
            }

            // quitar de ambos mapas
            String nombreNorm = almacen.normalizarNombre(pista.getNombre());
            almacen.idPistaPorNombre.remove(nombreNorm);
            almacen.pistasPorId.remove(courtId);
        }

        // ========= HANDLER 400 (como en el carrito) =========
        @ExceptionHandler(ExcepcionPistaIncorrecta.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public List<ModeloCampoIncorrecto> pistaIncorrecta(ExcepcionPistaIncorrecta ex) {
            return ex.getErrores().stream().map(error ->
                    new ModeloCampoIncorrecto(
                            error.getDefaultMessage(),
                            error.getField(),
                            error.getRejectedValue()
                    )
            ).toList();
        }

    }
