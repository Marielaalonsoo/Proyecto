package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Excepciones.ExcepcionDatosIncorrectos;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ManejadorErroresGlobales {

    @ExceptionHandler(ExcepcionDatosIncorrectos.class)
    public ResponseEntity<?> datosIncorrectos(ExcepcionDatosIncorrectos ex) {
        return ResponseEntity.badRequest().body(ex.getErrores());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> errorLanzado(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason()); // Explica el error
    }
}
