package edu.comillas.icai.gitt.pat.spring.PistaPadel.excepciones;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.util.List;

public class ExcepcionPistaIncorrecta extends RuntimeException{

    private final List<FieldError> errores;

    public ExcepcionPistaIncorrecta(BindingResult result) {
        this.errores = result.getFieldErrors();
    }

    public List<FieldError> getErrores() {
        return errores;
    }
}


