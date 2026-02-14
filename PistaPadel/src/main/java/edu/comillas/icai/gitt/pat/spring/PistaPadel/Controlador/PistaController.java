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
        if (almacen.idPistaPorNombre.containsKey(nombre)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nombre de pista duplicado");
        }
        Integer id = almacen.generarIdPista(); //generar un idpista
        boolean activa = (pistaNueva.activa() == null) ? true : pistaNueva.activa();

        Pista pista = new Pista(id, pistaNueva.nombre(), pistaNueva.ubicacion(), activa, LocalDate.now());

        almacen.pistasPorId.put(id, pista);
        almacen.idPistaPorNombre.put(nombre, id);

        return pista;

    }


}