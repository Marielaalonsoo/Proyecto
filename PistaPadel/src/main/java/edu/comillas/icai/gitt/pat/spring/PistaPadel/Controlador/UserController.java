package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Excepciones.ExcepcionDatosIncorrectos;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloUsuarioPatch;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/pistaPadel/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsers(Principal principal) {
        return ResponseEntity.ok(userService.getUsers(principal));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Integer userId, Principal principal) {
        return ResponseEntity.ok(userService.getUser(userId, principal));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<?> patchUser(@PathVariable Integer userId,
                                       @Valid @RequestBody ModeloUsuarioPatch req,
                                       BindingResult br,
                                       Principal principal) {
        if (br.hasErrors()) {
            throw new ExcepcionDatosIncorrectos(br);
        }
        return ResponseEntity.ok(userService.patchUser(userId, req, principal));
    }
}