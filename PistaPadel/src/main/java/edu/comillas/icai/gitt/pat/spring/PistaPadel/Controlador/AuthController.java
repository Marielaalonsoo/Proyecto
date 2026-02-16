package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Almacen.AlmacenMemoria;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Excepciones.ExcepcionDatosIncorrectos;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloLogin;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloUsuario;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Usuario;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    // 201 / 400 (validación) / 409 (email duplicado)
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

        // guardamos el email ya normalizado para evitar duplicados raros
        u.setEmail(emailNorm);

        u.setPassword(req.password());
        u.setTelefono(req.telefono() == null ? "" : req.telefono().trim());

        u.setRol(Rol.USER);
        u.setFechaRegistro(LocalDateTime.now());
        u.setActivo(true);

        almacen.guardarUsuario(u);

        logger.info("Usuario registrado: id={}, email={}", u.getIdUsuario(), u.getEmail());

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

    // 200 / 400 (validación) / 401 (credenciales)
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody ModeloLogin req, BindingResult result, HttpSession session) {

        if (result.hasErrors()) {
            throw new ExcepcionDatosIncorrectos(result);
        }

        String emailNorm = almacen.normalizarEmail(req.email());
        Usuario u = almacen.buscarUsuarioPorEmail(emailNorm);

        if (u == null || !u.isActivo() || !req.password().equals(u.getPassword())) {
            logger.info("Login fallido: {}", emailNorm);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        session.setAttribute("idUsuario", u.getIdUsuario());
        logger.info("Login correcto: id={}, email={}", u.getIdUsuario(), u.getEmail());

        return ResponseEntity.ok(Map.of("ok", true));
    }

    // 200 / 401
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {

        Object idObj = session.getAttribute("idUsuario");
        if (!(idObj instanceof Integer)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Integer id = (Integer) idObj;
        Usuario u = almacen.buscarUsuarioPorId(id);

        if (u == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

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
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {

        Object idObj = session.getAttribute("idUsuario");
        if (idObj == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        session.invalidate();
        logger.info("Logout");

        return ResponseEntity.noContent().build();
    }
}
