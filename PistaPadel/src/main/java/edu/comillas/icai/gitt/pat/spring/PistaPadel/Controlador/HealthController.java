package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pistaPadel")
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
