package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Excepciones.ExcepcionDatosIncorrectos;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorErroresGlobales {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> manejarStatus(ResponseStatusException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", ex.getStatusCode().value());
        body.put("message", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(ExcepcionDatosIncorrectos.class)
    public ResponseEntity<?> manejarDatosIncorrectos(ExcepcionDatosIncorrectos ex) {
        return ResponseEntity.badRequest().body(ex.getErrores());
    }
}
