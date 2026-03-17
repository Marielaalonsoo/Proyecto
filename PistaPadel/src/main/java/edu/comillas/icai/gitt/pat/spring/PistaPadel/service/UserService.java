package edu.comillas.icai.gitt.pat.spring.PistaPadel.service;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloUsuarioPatch;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio.RepoUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final RepoUsuario repoUsuario;
    private final AuthService authService;

    public UserService(RepoUsuario repoUsuario, AuthService authService) {
        this.repoUsuario = repoUsuario;
        this.authService = authService;
    }

    public List<Map<String, Object>> getUsers(Principal principal) {
        Usuario actual = authService.getUsuarioAutenticado(principal);

        if (!esAdmin(actual)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        List<Usuario> todos = new ArrayList<>();
        repoUsuario.findAll().forEach(todos::add);
        todos.sort(Comparator.comparing(Usuario::getIdUsuario));

        List<Map<String, Object>> salida = new ArrayList<>();
        for (Usuario u : todos) {
            salida.add(authService.usuarioToMap(u));
        }

        logger.debug("ADMIN lista usuarios: count={}", salida.size());
        return salida;
    }

    public Map<String, Object> getUser(Integer userId, Principal principal) {
        Usuario actual = authService.getUsuarioAutenticado(principal);

        boolean admin = esAdmin(actual);
        boolean dueno = actual.getIdUsuario().equals(userId);

        if (!admin && !dueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        Usuario u = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no existe"));

        return authService.usuarioToMap(u);
    }

    public Map<String, Object> patchUser(Integer userId, ModeloUsuarioPatch req, Principal principal) {
        Usuario actual = authService.getUsuarioAutenticado(principal);

        boolean admin = esAdmin(actual);
        boolean dueno = actual.getIdUsuario().equals(userId);

        if (!admin && !dueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        Usuario u = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no existe"));

        if (req.nombre() != null) {
            String nombre = req.nombre().trim();
            if (nombre.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre vacío");
            }
            u.setNombre(nombre);
        }

        if (req.apellidos() != null) {
            u.setApellidos(req.apellidos().trim());
        }

        if (req.telefono() != null) {
            u.setTelefono(req.telefono().trim());
        }

        if (req.email() != null) {
            String nuevoEmail = req.email().toLowerCase().trim();
            String emailActual = u.getEmail().toLowerCase().trim();

            if (!nuevoEmail.equals(emailActual)) {
                Usuario existente = repoUsuario.findByEmailIgnoreCase(nuevoEmail).orElse(null);
                if (existente != null && !existente.getIdUsuario().equals(u.getIdUsuario())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email duplicado");
                }
                u.setEmail(nuevoEmail);
            }
        }

        Usuario guardado = repoUsuario.save(u);

        logger.info("Usuario actualizado: id={}, por={}", guardado.getIdUsuario(), actual.getIdUsuario());
        return authService.usuarioToMap(guardado);
    }

    private boolean esAdmin(Usuario u) {
        return u.getRol() == Rol.ADMIN;
    }
}