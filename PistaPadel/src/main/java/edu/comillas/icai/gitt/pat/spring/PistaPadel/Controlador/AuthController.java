package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Almacen.AlmacenMemoria;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Excepciones.ExcepcionDatosIncorrectos;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pistaPadel/auth")
public class AuthController {

    private final AlmacenMemoria almacen = AlmacenMemoria.getAlmacen();

    // 400
    @ExceptionHandler(ExcepcionDatosIncorrectos.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<ModeloCampoIncorrecto> datosIncorrectos(ExcepcionDatosIncorrectos ex) {
        return ex.getErrores().stream().map(err ->
                new ModeloCampoIncorrecto(
                        err.getDefaultMessage(),
                        err.getField(),
                        err.getRejectedValue()
                )
        ).toList();
    }

    // 201 / 400 (validación) / 409 (email duplicado)
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody ModeloUsuario req, BindingResult result) {

        if (result.hasErrors()) {
            throw new ExcepcionDatosIncorrectos(result);
        }

        String emailNorm = almacen.normalizarEmail(req.email());

        // 409
        if (almacen.idUsuarioPorEmail.containsKey(emailNorm)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        Usuario u = new Usuario();
        u.setIdUsuario(almacen.generarIdUsuario());
        u.setNombre(req.nombre().trim());

        if (req.apellidos() == null) u.setApellidos("");
        else u.setApellidos(req.apellidos().trim());

        u.setEmail(req.email().trim());
        u.setPassword(req.password());

        if (req.telefono() == null) u.setTelefono("");
        else u.setTelefono(req.telefono().trim());

        u.setRol(Rol.USER);
        u.setFechaRegistro(LocalDateTime.now());
        u.setActivo(true);

        almacen.usuariosPorId.put(u.getIdUsuario(), u);
        almacen.idUsuarioPorEmail.put(emailNorm, u.getIdUsuario());

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
        Integer id = almacen.idUsuarioPorEmail.get(emailNorm);

        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Usuario u = almacen.usuariosPorId.get(id);
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (!u.isActivo()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (!req.password().equals(u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        session.setAttribute("idUsuario", u.getIdUsuario());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // 200 / 401
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {

        Object idObj = session.getAttribute("idUsuario");
        if (idObj == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Integer id;
        try {
            id = (Integer) idObj;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Usuario u = almacen.usuariosPorId.get(id);
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
        return ResponseEntity.noContent().build();
    }
}
