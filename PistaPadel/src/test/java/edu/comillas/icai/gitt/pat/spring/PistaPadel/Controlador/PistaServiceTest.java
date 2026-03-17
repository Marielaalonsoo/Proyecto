package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.ModeloPistaCrear;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.service.PistaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PistaServiceTest {

    private PistaService pistaService;
    private RepoPistaFake repoPista;

    @BeforeEach
    void setUp() {
        repoPista = new RepoPistaFake();
        pistaService = new PistaService(repoPista);
    }

    @Test
    void crearPistaOkTest() {
        ModeloPistaCrear req = new ModeloPistaCrear("Pista 1", "Club Central", 20, true, LocalDate.of(2026, 3, 17));

        Pista pista = pistaService.crear(req);

        assertNotNull(pista);
        assertEquals("Pista 1", pista.getNombre());
        assertEquals(20, pista.getPrecioHora());
    }

    @Test
    void crearPistaDuplicadaTest() {
        Pista existente = new Pista();
        existente.setIdPista(1);
        existente.setNombre("Pista 1");
        existente.setUbicacion("Club Central");
        existente.setPrecioHora(20);
        existente.setActiva(true);
        existente.setFechaAlta(LocalDate.now());
        repoPista.save(existente);

        ModeloPistaCrear req = new ModeloPistaCrear("Pista 1", "Otra ubicacion", 25, true, LocalDate.of(2026, 3, 17));

        assertThrows(ResponseStatusException.class, () -> pistaService.crear(req));
    }

    static class RepoPistaFake implements RepoPista {
        private final List<Pista> pistas = new ArrayList<>();

        @Override
        public Optional<Pista> findByNombreIgnoreCase(String nombre) {
            return pistas.stream().filter(p -> p.getNombre().equalsIgnoreCase(nombre)).findFirst();
        }

        @Override
        public boolean existsByNombreIgnoreCase(String nombre) {
            return pistas.stream().anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre));
        }

        @Override
        public List<Pista> findByActiva(boolean activa) {
            return pistas.stream().filter(p -> p.isActiva() == activa).toList();
        }

        @Override
        public <S extends Pista> S save(S entity) {
            if (entity.getIdPista() == null) entity.setIdPista(pistas.size() + 1);
            pistas.removeIf(p -> p.getIdPista().equals(entity.getIdPista()));
            pistas.add(entity);
            return entity;
        }

        @Override public Optional<Pista> findById(Integer integer) { return pistas.stream().filter(p -> p.getIdPista().equals(integer)).findFirst(); }
        @Override public Iterable<Pista> findAll() { return pistas; }
        @Override public boolean existsById(Integer integer) { return pistas.stream().anyMatch(p -> p.getIdPista().equals(integer)); }
        @Override public long count() { return pistas.size(); }
        @Override public void deleteById(Integer integer) {}
        @Override public void delete(Pista entity) {}
        @Override public void deleteAllById(Iterable<? extends Integer> integers) {}
        @Override public void deleteAll(Iterable<? extends Pista> entities) {}
        @Override public void deleteAll() { pistas.clear(); }
        @Override public <S extends Pista> Iterable<S> saveAll(Iterable<S> entities) { return entities; }
        @Override public Iterable<Pista> findAllById(Iterable<Integer> integers) { return pistas; }
    }
}