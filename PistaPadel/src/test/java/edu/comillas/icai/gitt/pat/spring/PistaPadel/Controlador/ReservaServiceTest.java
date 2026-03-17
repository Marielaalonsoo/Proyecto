package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.*;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio.RepoPista;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio.RepoReserva;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio.RepoUsuario;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReservaServiceTest {

    private ReservaService reservaService;
    private RepoReservaFake repoReserva;
    private RepoUsuarioFake repoUsuario;
    private RepoPistaFake repoPista;

    private Usuario usuario;
    private Pista pista;
    private Principal principal;

    @BeforeEach
    void setUp() {
        repoReserva = new RepoReservaFake();
        repoUsuario = new RepoUsuarioFake();
        repoPista = new RepoPistaFake();

        reservaService = new ReservaService(repoReserva, repoUsuario, repoPista);

        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNombre("Juan");
        usuario.setApellidos("Perez");
        usuario.setEmail("juan@padel.com");
        usuario.setPasswordHash("hash");
        usuario.setTelefono("600000000");
        usuario.setRol(Rol.USER);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setActivo(true);

        pista = new Pista();
        pista.setIdPista(1);
        pista.setNombre("Pista 1");
        pista.setUbicacion("Club Central");
        pista.setPrecioHora(20);
        pista.setActiva(true);
        pista.setFechaAlta(LocalDate.now());

        repoUsuario.guardar(usuario);
        repoPista.guardar(pista);

        principal = () -> "juan@padel.com";
    }

    @Test
    void crearReservaOkTest() {
        ModeloReserva body = new ModeloReserva(1, LocalDate.of(2026, 3, 20), LocalTime.of(18, 0), 90);

        Reserva reserva = reservaService.crearReserva(body, principal);

        assertNotNull(reserva);
        assertEquals(usuario.getEmail(), reserva.getUsuario().getEmail());
        assertEquals(pista.getNombre(), reserva.getPista().getNombre());
        assertEquals(EstadoReserva.ACTIVA, reserva.getEstado());
        assertEquals(LocalTime.of(19, 30), reserva.getHoraFin());
    }

    @Test
    void crearReservaSlotOcupadoTest() {
        Reserva existente = new Reserva(
                10,
                usuario,
                pista,
                LocalDate.of(2026, 3, 20),
                LocalTime.of(18, 30),
                60,
                EstadoReserva.ACTIVA,
                LocalDateTime.now()
        );
        repoReserva.save(existente);

        ModeloReserva body = new ModeloReserva(1, LocalDate.of(2026, 3, 20), LocalTime.of(18, 0), 90);

        assertThrows(ResponseStatusException.class, () -> reservaService.crearReserva(body, principal));
    }

    static class RepoUsuarioFake implements RepoUsuario {
        private final List<Usuario> usuarios = new ArrayList<>();

        void guardar(Usuario u) { usuarios.add(u); }

        @Override
        public Optional<Usuario> findByEmailIgnoreCase(String email) {
            return usuarios.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst();
        }

        @Override
        public boolean existsByEmailIgnoreCase(String email) {
            return usuarios.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        }

        @Override
        public <S extends Usuario> S save(S entity) {
            if (entity.getIdUsuario() == null) entity.setIdUsuario(usuarios.size() + 1);
            usuarios.removeIf(u -> u.getIdUsuario().equals(entity.getIdUsuario()));
            usuarios.add(entity);
            return entity;
        }

        @Override public Optional<Usuario> findById(Integer integer) { return usuarios.stream().filter(u -> u.getIdUsuario().equals(integer)).findFirst(); }
        @Override public Iterable<Usuario> findAll() { return usuarios; }
        @Override public boolean existsById(Integer integer) { return usuarios.stream().anyMatch(u -> u.getIdUsuario().equals(integer)); }
        @Override public long count() { return usuarios.size(); }
        @Override public void deleteById(Integer integer) {}
        @Override public void delete(Usuario entity) {}
        @Override public void deleteAllById(Iterable<? extends Integer> integers) {}
        @Override public void deleteAll(Iterable<? extends Usuario> entities) {}
        @Override public void deleteAll() { usuarios.clear(); }
        @Override public <S extends Usuario> Iterable<S> saveAll(Iterable<S> entities) { return entities; }
        @Override public Iterable<Usuario> findAllById(Iterable<Integer> integers) { return usuarios; }
    }

    static class RepoPistaFake implements RepoPista {
        private final List<Pista> pistas = new ArrayList<>();

        void guardar(Pista p) { pistas.add(p); }

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

    static class RepoReservaFake implements RepoReserva {
        private final List<Reserva> reservas = new ArrayList<>();

        @Override
        public List<Reserva> findByUsuario_IdUsuarioOrderByFechaReservaAscHoraInicioAsc(Integer idUsuario) {
            return reservas.stream().filter(r -> r.getUsuario().getIdUsuario().equals(idUsuario)).toList();
        }

        @Override
        public List<Reserva> findByPista_IdPistaAndFechaReservaAndEstadoOrderByHoraInicioAsc(Integer idPista, LocalDate fechaReserva, EstadoReserva estado) {
            return reservas.stream()
                    .filter(r -> r.getPista().getIdPista().equals(idPista)
                            && r.getFechaReserva().equals(fechaReserva)
                            && r.getEstado() == estado)
                    .toList();
        }

        @Override
        public <S extends Reserva> S save(S entity) {
            if (entity.getIdReserva() == null) entity.setIdReserva(reservas.size() + 1);
            reservas.removeIf(r -> r.getIdReserva().equals(entity.getIdReserva()));
            reservas.add(entity);
            return entity;
        }

        @Override public Optional<Reserva> findById(Integer integer) { return reservas.stream().filter(r -> r.getIdReserva().equals(integer)).findFirst(); }
        @Override public Iterable<Reserva> findAll() { return reservas; }
        @Override public boolean existsById(Integer integer) { return reservas.stream().anyMatch(r -> r.getIdReserva().equals(integer)); }
        @Override public long count() { return reservas.size(); }
        @Override public void deleteById(Integer integer) {}
        @Override public void delete(Reserva entity) {}
        @Override public void deleteAllById(Iterable<? extends Integer> integers) {}
        @Override public void deleteAll(Iterable<? extends Reserva> entities) {}
        @Override public void deleteAll() { reservas.clear(); }
        @Override public <S extends Reserva> Iterable<S> saveAll(Iterable<S> entities) { return entities; }
        @Override public Iterable<Reserva> findAllById(Iterable<Integer> integers) { return reservas; }
    }
}