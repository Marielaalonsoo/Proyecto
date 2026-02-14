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

import java.util.*;

@RestController
@RequestMapping("/pistaPadel/users")
public class UserController {

    private final AlmacenMemoria almacen = AlmacenMemoria.getAlmacen();

    private Usuario getUsuarioLogueado(HttpSession session) {
        Object aux = session.getAttribute("idUsuario");
        if (aux == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Integer id;
        try {
            id = (Integer) aux;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Usuario u = almacen.usuariosPorId.get(id);
        if (u == null || !u.isActivo()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return u;
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
    public ResponseEntity<?> getUsers(HttpSession session) {

        Usuario u = getUsuarioLogueado(session);
        if (u.getRol() != Rol.ADMIN) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        List<Usuario> todos = new ArrayList<>(almacen.usuariosPorId.values());
        todos.sort(Comparator.comparing(Usuario::getIdUsuario));

        List<Map<String, Object>> lista = new ArrayList<>();
        for (Usuario x : todos) lista.add(limpiar(x));

        return ResponseEntity.ok(lista);
    }

    // (ADMIN o dueño) GET /pistaPadel/users/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Integer userId, HttpSession session) {

        Usuario u = getUsuarioLogueado(session);

        if (u.getRol() != Rol.ADMIN && !u.getIdUsuario().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Usuario x = almacen.usuariosPorId.get(userId);
        if (x == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return ResponseEntity.ok(limpiar(x));
    }

    // (ADMIN o dueño) PATCH /pistaPadel/users/{userId}
    @PatchMapping("/{userId}")
    public ResponseEntity<?> patchUser(@PathVariable Integer userId,
                                       @Valid @RequestBody ModeloUsuarioPatch req,
                                       BindingResult br,
                                       HttpSession session) {

        if (br.hasErrors()) throw new ExcepcionDatosIncorrectos(br);

        Usuario u = getUsuarioLogueado(session);
        if (u.getRol() != Rol.ADMIN && !u.getIdUsuario().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Usuario x = almacen.usuariosPorId.get(userId);
        if (x == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        // nombre
        if (req.nombre() != null) {
            String nombre = req.nombre().trim();
            if (nombre.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            x.setNombre(nombre);
        }

        // apellidos
        if (req.apellidos() != null) {
            x.setApellidos(req.apellidos().trim());
        }

        // telefono
        if (req.telefono() != null) {
            x.setTelefono(req.telefono().trim());
        }

        // email + 409 si duplicado
        if (req.email() != null) {
            String nuevo = req.email().trim();
            String nuevoNorm = almacen.normalizarEmail(nuevo);
            String actualNorm = almacen.normalizarEmail(x.getEmail());

            if (!nuevoNorm.equals(actualNorm)) {
                Integer idExistente = almacen.idUsuarioPorEmail.get(nuevoNorm);
                if (idExistente != null && !idExistente.equals(x.getIdUsuario())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT);
                }
                almacen.idUsuarioPorEmail.remove(actualNorm);
                almacen.idUsuarioPorEmail.put(nuevoNorm, x.getIdUsuario());
                x.setEmail(nuevo);
            }
        }

        return ResponseEntity.ok(limpiar(x));
    }
}
