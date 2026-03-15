package edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Pista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface RepoPista extends JpaRepository<Pista, Integer> {
    Optional<Pista> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
    List<Pista> findByActiva(boolean activa);
}
