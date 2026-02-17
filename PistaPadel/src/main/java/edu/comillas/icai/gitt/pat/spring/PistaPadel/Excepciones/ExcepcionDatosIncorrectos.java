package edu.comillas.icai.gitt.pat.spring.PistaPadel.Excepciones;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloCampoIncorrecto;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

public class ExcepcionDatosIncorrectos extends RuntimeException {

    private final List<ModeloCampoIncorrecto> errores = new ArrayList<>();

    public ExcepcionDatosIncorrectos(BindingResult br) {
        for (FieldError fe : br.getFieldErrors()) {
            errores.add(new ModeloCampoIncorrecto(
                    fe.getDefaultMessage(),
                    fe.getField(),
                    fe.getRejectedValue()
            ));
        }
    }

    public List<ModeloCampoIncorrecto> getErrores() {
        return errores;
    }
}
