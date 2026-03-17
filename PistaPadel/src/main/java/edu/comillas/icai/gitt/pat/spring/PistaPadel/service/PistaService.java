package edu.comillas.icai.gitt.pat.spring.PistaPadel.service;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloPistaCrear;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloPistaPatch;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio.RepoPista;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PistaService {

    private final RepoPista repoPista;

    public PistaService(RepoPista repoPista) {
        this.repoPista = repoPista;
    }

    public List<Pista> listar(Boolean active) {
        List<Pista> res = new ArrayList<>();

        if (active != null) {
            res = repoPista.findByActiva(active);
        } else {
            repoPista.findAll().forEach(res::add);
        }

        res.sort(Comparator.comparing(Pista::getIdPista));
        return res;
    }

    public Pista detalle(int courtId) {
        return repoPista.findById(courtId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe"));
    }

    public Pista crear(ModeloPistaCrear req) {
        if (req.precioHora() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "precioHora inválido");
        }

        String nombre = req.nombre().trim();

        if (repoPista.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una pista con ese nombre");
        }

        Pista nueva = new Pista();
        nueva.setIdPista(null);
        nueva.setNombre(nombre);
        nueva.setUbicacion(req.ubicacion().trim());
        nueva.setPrecioHora(req.precioHora());
        nueva.setActiva(req.activa());
        nueva.setFechaAlta(req.fechaAlta() != null ? req.fechaAlta() : LocalDate.now());

        return repoPista.save(nueva);
    }

    public Pista modificar(int courtId, ModeloPistaPatch cambios) {
        Pista actual = repoPista.findById(courtId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe"));

        if (cambios == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body vacío");
        }

        if (cambios.nombre() != null && !cambios.nombre().isBlank()) {
            String nuevoNombre = cambios.nombre().trim();

            Pista otra = repoPista.findByNombreIgnoreCase(nuevoNombre).orElse(null);
            if (otra != null && !otra.getIdPista().equals(courtId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una pista con ese nombre");
            }

            actual.setNombre(nuevoNombre);
        }

        if (cambios.ubicacion() != null && !cambios.ubicacion().isBlank()) {
            actual.setUbicacion(cambios.ubicacion().trim());
        }

        if (cambios.fechaAlta() != null) {
            actual.setFechaAlta(cambios.fechaAlta());
        }

        if (cambios.activa() != null) {
            actual.setActiva(cambios.activa());
        }

        if (cambios.precioHora() != null) {
            if (cambios.precioHora() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "precioHora inválido");
            }
            actual.setPrecioHora(cambios.precioHora());
        }

        return repoPista.save(actual);
    }

    public void desactivarOBorrar(int courtId) {
        Pista pista = repoPista.findById(courtId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no existe"));

        pista.setActiva(false);
        repoPista.save(pista);
    }
}