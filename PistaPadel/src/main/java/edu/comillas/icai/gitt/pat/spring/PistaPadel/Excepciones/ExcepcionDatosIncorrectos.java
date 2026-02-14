package edu.comillas.icai.gitt.pat.spring.PistaPadel.Excepciones;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

public class ExcepcionDatosIncorrectos extends RuntimeException {

    private final List<FieldError> errores;

    public ExcepcionDatosIncorrectos(BindingResult result) {
        this.errores = result.getFieldErrors();
    }

    public List<FieldError> getErrores() {
        return errores;
    }
}
