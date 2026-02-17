package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Almacen.AlmacenMemoria;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Excepciones.ExcepcionDatosIncorrectos;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloUsuarioPatch;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Usuario;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/pistaPadel/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final AlmacenMemoria almacen;

    public UserController(AlmacenMemoria almacen) {
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
        if (u != null) {
            if (!u.isActivo()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            return u;
        }

        // Entrega 1: si no existe, lo creamos para que el sistema sea usable
        Usuario nuevo = new Usuario();
        nuevo.setIdUsuario(almacen.generarIdUsuario());
        nuevo.setNombre(username);
        nuevo.setApellidos("");
        nuevo.setEmail(emailNorm);
        nuevo.setPassword("");
        nuevo.setTelefono("");
        nuevo.setRol(Rol.USER);
        nuevo.setFechaRegistro(LocalDateTime.now());
        nuevo.setActivo(true);

        almacen.guardarUsuario(nuevo);
        return nuevo;
    }

    private boolean esAdmin(Principal principal) {
        // Para entrega 1: el admin de teoría se llama "admin"
        return principal != null && "admin".equalsIgnoreCase(principal.getName());
    }

    private Map<String, Object> limpiar(Usuario u) {
        Map<String, Object> m = new HashMap<>();
        m.put("idUsuario", u.getIdUsuario());
        m.put("nombre", u.getNombre());
        m.put("apellidos", u.getApellidos());
        m.put("email", u.getEmail());
        m.put("telefono", u.getTelefono());
        m.put("rol", u.getRol().toString());
        m.put("activo", u.isActivo());
        m.put("fechaRegistro", u.getFechaRegistro());
        return m;
    }

    // (ADMIN) GET /pistaPadel/users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsers(Principal principal) {

        // Para que el "admin" exista también en memoria (si alguien lo lista el primero)
        Usuario admin = getUsuarioAutenticado(principal);
        admin.setRol(Rol.ADMIN);

        List<Usuario> todos = new ArrayList<>(almacen.listarUsuarios());
        todos.sort(Comparator.comparing(Usuario::getIdUsuario));

        List<Map<String, Object>> salida = new ArrayList<>();
        for (Usuario x : todos) {
            salida.add(limpiar(x));
        }

        logger.debug("ADMIN lista usuarios: count={}", salida.size());
        return ResponseEntity.ok(salida);
    }

    // (ADMIN o dueño) GET /pistaPadel/users/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Integer userId, Principal principal) {

        Usuario actual = getUsuarioAutenticado(principal);

        boolean admin = esAdmin(principal);
        boolean dueno = actual.getIdUsuario().equals(userId);

        if (!admin && !dueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Usuario x = almacen.buscarUsuarioPorId(userId);
        if (x == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(limpiar(x));
    }

    // (ADMIN o dueño) PATCH /pistaPadel/users/{userId}
    @PatchMapping("/{userId}")
    public ResponseEntity<?> patchUser(@PathVariable Integer userId,
                                       @Valid @RequestBody ModeloUsuarioPatch req,
                                       BindingResult br,
                                       Principal principal) {

        if (br.hasErrors()) {
            throw new ExcepcionDatosIncorrectos(br);
        }

        Usuario actual = getUsuarioAutenticado(principal);

        boolean admin = esAdmin(principal);
        boolean dueno = actual.getIdUsuario().equals(userId);

        if (!admin && !dueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Usuario x = almacen.buscarUsuarioPorId(userId);
        if (x == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (req.nombre() != null) {
            String nombre = req.nombre().trim();
            if (nombre.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            x.setNombre(nombre);
        }

        if (req.apellidos() != null) {
            x.setApellidos(req.apellidos().trim());
        }

        if (req.telefono() != null) {
            x.setTelefono(req.telefono().trim());
        }

        if (req.email() != null) {
            String nuevoNorm = almacen.normalizarEmail(req.email());
            String actualNorm = almacen.normalizarEmail(x.getEmail());

            if (!nuevoNorm.equals(actualNorm)) {
                Usuario existente = almacen.buscarUsuarioPorEmail(nuevoNorm);
                if (existente != null && !existente.getIdUsuario().equals(x.getIdUsuario())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT);
                }
                x.setEmail(nuevoNorm);
            }
        }

        logger.info("Usuario actualizado: id={}, por={}", x.getIdUsuario(), actual.getIdUsuario());
        return ResponseEntity.ok(limpiar(x));
    }
}
