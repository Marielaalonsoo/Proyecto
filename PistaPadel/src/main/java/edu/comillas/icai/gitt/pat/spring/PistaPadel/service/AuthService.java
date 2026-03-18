package edu.comillas.icai.gitt.pat.spring.PistaPadel.service;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloLogin;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloUsuario;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio.RepoUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final RepoUsuario repoUsuario;
    private final PasswordEncoder passwordEncoder;

    public AuthService(RepoUsuario repoUsuario, PasswordEncoder passwordEncoder) {
        this.repoUsuario = repoUsuario;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> register(ModeloUsuario req) {
        String emailNorm = normalizarEmail(req.email());

        if (repoUsuario.existsByEmailIgnoreCase(emailNorm)) {
            logger.info("Registro rechazado por email duplicado: {}", emailNorm);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email duplicado");
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
        return usuarioToMap(guardado);
    }

    public Map<String, Object> login(ModeloLogin req, Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        Usuario u = getUsuarioAutenticado(principal);

        logger.info("Login correcto: userId={}, email={}", u.getIdUsuario(), u.getEmail());

        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("usuario", usuarioToMap(u));
        res.put("mensaje", "Autenticado con Basic Auth");
        return res;
    }

    public Map<String, Object> me(Principal principal) {
        Usuario u = getUsuarioAutenticado(principal);
        return usuarioToMap(u);
    }

    public void logout(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        logger.info("Logout solicitado por {}", principal.getName());
        // Con Basic Auth no hay sesión de servidor que invalidar.
    }

    public Usuario getUsuarioAutenticado(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        String emailNorm = normalizarEmail(principal.getName());

        Usuario u = repoUsuario.findByEmailIgnoreCase(emailNorm)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        if (!u.isActivo()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario inactivo");
        }

        return u;
    }

    private String normalizarEmail(String email) {
        return email.toLowerCase().trim();
    }

    public Map<String, Object> usuarioToMap(Usuario u) {
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
}