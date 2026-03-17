package edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Pista;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RepoPista extends CrudRepository<Pista, Integer> {
    Optional<Pista> findByNombreIgnoreCase(String nombre); //buscan por nombre
    boolean existsByNombreIgnoreCase(String nombre);
    List<Pista> findByActiva(boolean activa); //te devuelve las pistas operativas al momento (true)
}
