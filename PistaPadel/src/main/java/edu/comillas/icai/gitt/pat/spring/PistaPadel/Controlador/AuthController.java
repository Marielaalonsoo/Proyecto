package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Almacen.AlmacenMemoria;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pistaPadel/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AlmacenMemoria almacen;

    public AuthController(AlmacenMemoria almacen) {
        this.almacen = almacen;
    }

    private Usuario getUsuarioAutenticado(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String username = principal.getName().trim();
        if (username.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String emailNorm = almacen.normalizarEmail(username);

        Usuario u = almacen.buscarUsuarioPorEmail(emailNorm);
        if (u != null) return u;

        Usuario nuevo = new Usuario();
        nuevo.setIdUsuario(almacen.generarIdUsuario());
        nuevo.setNombre(username);
        nuevo.setApellidos("");
        nuevo.setEmail(emailNorm);
        nuevo.setPassword("");
        nuevo.setTelefono("");
        nuevo.setRol("admin".equalsIgnoreCase(username) ? Rol.ADMIN : Rol.USER);
        nuevo.setFechaRegistro(LocalDateTime.now());
        nuevo.setActivo(true);

        almacen.guardarUsuario(nuevo);
        return nuevo;
    }

    // 201 / 400 / 409
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody ModeloUsuario req, BindingResult result) {

        if (result.hasErrors()) {
            throw new ExcepcionDatosIncorrectos(result);
        }

        String emailNorm = almacen.normalizarEmail(req.email());

        if (almacen.buscarUsuarioPorEmail(emailNorm) != null) {
            logger.info("Registro rechazado por email duplicado: {}", emailNorm);
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        Usuario u = new Usuario();
        u.setIdUsuario(almacen.generarIdUsuario());
        u.setNombre(req.nombre().trim());
        u.setApellidos(req.apellidos() == null ? "" : req.apellidos().trim());
        u.setEmail(emailNorm);
        u.setPassword(req.password()); // se guarda por si queréis usarlo más adelante
        u.setTelefono(req.telefono() == null ? "" : req.telefono().trim());
        u.setRol(Rol.USER);
        u.setFechaRegistro(LocalDateTime.now());
        u.setActivo(true);

        almacen.guardarUsuario(u);

        Map<String, Object> res = new HashMap<>();
        res.put("idUsuario", u.getIdUsuario());
        res.put("nombre", u.getNombre());
        res.put("apellidos", u.getApellidos());
        res.put("email", u.getEmail());
        res.put("telefono", u.getTelefono());
        res.put("rol", u.getRol().toString());
        res.put("activo", u.isActivo());

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // 200 / 400 / 401
    // En seguridad de teoría, quien autentica es Spring. Aquí devolvemos ok y listo.
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

        // Asegura que el usuario exista en memoria (entrega 1)
        Usuario u = getUsuarioAutenticado(principal);

        logger.info("Login (Spring Security): userId={}, name={}", u.getIdUsuario(), principal.getName());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // 200 / 401
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

    // 204 / 401
    // En Basic Auth no hay sesión que invalidar; si estás autenticado, devolvemos 204.
    @PostMapping("/logout")
    public ResponseEntity<?> logout(Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        logger.info("Logout (sin sesión): {}", principal.getName());
        return ResponseEntity.noContent().build();
    }
}
