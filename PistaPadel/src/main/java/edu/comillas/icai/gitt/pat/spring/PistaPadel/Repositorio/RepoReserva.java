package edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.EstadoReserva;
import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RepoReserva extends JpaRepository<Reserva, Integer> {

    List<Reserva> findByUsuario_IdUsuarioOrderByFechaReservaAscHoraInicioAsc(Integer idUsuario); //todas las reservas de un usuario en concreto

    List<Reserva> findByPista_IdPistaAndFechaReservaAndEstadoOrderByHoraInicioAsc( //para evitar sobre-reservas busca reservas en una pista concreta en un día concreto
            Integer idPista, LocalDate fechaReserva, EstadoReserva estado
    );
}