package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ManejadorErroresGlobales {

    @ResponseBody
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> errorLanzado(ResponseStatusException ex) {
        return new ResponseEntity<>(ex.getStatusCode());
    }
}
