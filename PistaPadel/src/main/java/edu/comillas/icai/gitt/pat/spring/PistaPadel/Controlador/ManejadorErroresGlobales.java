package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class ManejadorErroresGlobales {

    @ResponseBody
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> errorLanzado(ResponseStatusException ex) {
        // Igual que el ejemplo: devolver solo el status
        return new ResponseEntity<>(ex.getStatusCode());
    }
}
