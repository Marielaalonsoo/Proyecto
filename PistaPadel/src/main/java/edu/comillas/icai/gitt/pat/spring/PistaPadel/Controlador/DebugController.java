package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio.RepoReserva;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio.RepoUsuario;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DebugController {

    private final RepoUsuario repoUsuario;
    private final RepoPista repoPista;
    private final RepoReserva repoReserva;

    public DebugController(RepoUsuario repoUsuario, RepoPista repoPista, RepoReserva repoReserva) {
        this.repoUsuario = repoUsuario;
        this.repoPista = repoPista;
        this.repoReserva = repoReserva;
    }

    @GetMapping("/debug/db")
    public Map<String, Object> verBd() {
        Map<String, Object> res = new HashMap<>();

        ArrayList<Object> usuarios = new ArrayList<>();
        repoUsuario.findAll().forEach(usuarios::add);

        ArrayList<Object> pistas = new ArrayList<>();
        repoPista.findAll().forEach(pistas::add);

        ArrayList<Object> reservas = new ArrayList<>();
        repoReserva.findAll().forEach(reservas::add);

        res.put("usuarios", usuarios);
        res.put("pistas", pistas);
        res.put("reservas", reservas);

        return res;
    }
}