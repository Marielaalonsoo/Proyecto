package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio.RepoUsuario;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Excepciones.ExcepcionDatosIncorrectos;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloLogin;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloUsuario;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Usuario;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

//hay que cambiarlo
@RestController
@RequestMapping("/pistaPadel/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final RepoUsuario repoUsuario;
    private final PasswordEncoder passwordEncoder;

    public AuthController(RepoUsuario repoUsuario, PasswordEncoder passwordEncoder) {
        this.repoUsuario = repoUsuario;
        this.passwordEncoder = passwordEncoder;
    }

    private Usuario getUsuarioAutenticado(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String username = principal.getName().trim();
        if (username.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String emailNorm = username.toLowerCase().trim();

        Usuario u = repoUsuario.findByEmailIgnoreCase(emailNorm)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        if (!u.isActivo()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario inactivo");
        }

        return u;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody ModeloUsuario req, BindingResult result) {

        if (result.hasErrors()) {
            throw new ExcepcionDatosIncorrectos(result);
        }

        String emailNorm = req.email().toLowerCase().trim();

        if (repoUsuario.existsByEmailIgnoreCase(emailNorm)) {
            logger.info("Registro rechazado por email duplicado: {}", emailNorm);
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        Usuario u = new Usuario();
        u.setIdUsuario(null);
        u.setNombre(req.nombre().trim());
        u.setApellidos(req.apellidos() == null ? "" : req.apellidos().trim());
        u.setEmail(emailNorm);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setTelefono(req.telefono() == null ? "" : req.telefono().trim());
        u.setRol(Rol.USER);
        u.setFechaRegistro(LocalDateTime.now());
        u.setActivo(true);

        Usuario guardado = repoUsuario.save(u);

        Map<String, Object> res = new HashMap<>();
        res.put("idUsuario", guardado.getIdUsuario());
        res.put("nombre", guardado.getNombre());
        res.put("apellidos", guardado.getApellidos());
        res.put("email", guardado.getEmail());
        res.put("telefono", guardado.getTelefono());
        res.put("rol", guardado.getRol().toString());
        res.put("activo", guardado.isActivo());
        res.put("fechaRegistro", guardado.getFechaRegistro());

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody ModeloLogin req,
                                   BindingResult result,
                                   Principal principal) {

        if (result.hasErrors()) {
            throw new ExcepcionDatosIncorrectos(result);
        }

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Usuario u = getUsuarioAutenticado(principal);

        logger.info("Login (Spring Security): userId={}, name={}", u.getIdUsuario(), principal.getName());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Principal principal) {

        Usuario u = getUsuarioAutenticado(principal);

        Map<String, Object> res = new HashMap<>();
        res.put("idUsuario", u.getIdUsuario());
        res.put("nombre", u.getNombre());
        res.put("apellidos", u.getApellidos());
        res.put("email", u.getEmail());
        res.put("telefono", u.getTelefono());
        res.put("rol", u.getRol().toString());
        res.put("fechaRegistro", u.getFechaRegistro());
        res.put("activo", u.isActivo());

        return ResponseEntity.ok(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        logger.info("Logout (sin sesión): {}", principal.getName());
        return ResponseEntity.noContent().build();
    }
}