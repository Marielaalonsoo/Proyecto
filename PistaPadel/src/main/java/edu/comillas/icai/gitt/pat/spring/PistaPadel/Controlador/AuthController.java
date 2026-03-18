package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Excepciones.ExcepcionDatosIncorrectos;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloLogin;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloUsuario;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/pistaPadel/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody ModeloUsuario req, BindingResult result) {
        if (result.hasErrors()) {
            throw new ExcepcionDatosIncorrectos(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody ModeloLogin req, BindingResult result, Principal principal) {
        if (result.hasErrors()) {
            throw new ExcepcionDatosIncorrectos(result);
        }
        return ResponseEntity.ok(authService.login(req, principal));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Principal principal) {
        return ResponseEntity.ok(authService.me(principal));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Principal principal) {
        authService.logout(principal);
        return ResponseEntity.noContent().build();
    }
}