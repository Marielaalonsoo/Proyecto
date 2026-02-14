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

    private Usuario usuarioSesion(HttpSession session) {
        Object idObj = session.getAttribute("idUsuario");
        if (idObj == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Integer id = (Integer) idObj;
        Usuario u = almacen.usuariosPorId.get(id);

        if (u == null || !u.isActivo()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return u;
    }

    private Map<String, Object> respuesta(Usuario u) {
        Map<String, Object> res = new HashMap<>();
        res.put("idUsuario", u.getIdUsuario());
        res.put("nombre", u.getNombre());
        res.put("apellidos", u.getApellidos());
        res.put("email", u.getEmail());
        res.put("telefono", u.getTelefono());
        res.put("rol", u.getRol().toString());
        res.put("activo", u.isActivo());
        res.put("fechaRegistro", u.getFechaRegistro());
        return res;
    }

    @GetMapping
    public ResponseEntity<?> listar(HttpSession session) {
        Usuario u = usuarioSesion(session);
        if (u.getRol() != Rol.ADMIN) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        List<Map<String, Object>> out = new ArrayList<>();
        for (Usuario x : almacen.usuariosPorId.values()) {
            out.add(respuesta(x));
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> obtener(@PathVariable Integer userId, HttpSession session) {
        Usuario u = usuarioSesion(session);

        if (u.getRol() != Rol.ADMIN && !u.getIdUsuario().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Usuario x = almacen.usuariosPorId.get(userId);
        if (x == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return ResponseEntity.ok(respuesta(x));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<?> patch(@PathVariable Integer userId,
                                   @Valid @RequestBody ModeloUsuario req,
                                   BindingResult result,
                                   HttpSession session) {

        if (result.hasErrors()) throw new ExcepcionDatosIncorrectos(result);

        Usuario u = usuarioSesion(session);

        if (u.getRol() != Rol.ADMIN && !u.getIdUsuario().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Usuario x = almacen.usuariosPorId.get(userId);
        if (x == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        if (req.nombre() != null) {
            String n = req.nombre().trim();
            if (n.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            x.setNombre(n);
        }
        if (req.apellidos() != null) x.setApellidos(req.apellidos().trim());
        if (req.telefono() != null) x.setTelefono(req.telefono().trim());

        if (req.email() != null) {
            String nuevo = req.email().trim();
            String nuevoNorm = almacen.normalizarEmail(nuevo);
            String actualNorm = almacen.normalizarEmail(x.getEmail());

            if (!nuevoNorm.equals(actualNorm)) {
                Integer ya = almacen.idUsuarioPorEmail.get(nuevoNorm);
                if (ya != null && !ya.equals(x.getIdUsuario())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT);
                }
                almacen.idUsuarioPorEmail.remove(actualNorm);
                almacen.idUsuarioPorEmail.put(nuevoNorm, x.getIdUsuario());
                x.setEmail(nuevo);
            }
        }
        return ResponseEntity.ok(respuesta(x));
    }
}
