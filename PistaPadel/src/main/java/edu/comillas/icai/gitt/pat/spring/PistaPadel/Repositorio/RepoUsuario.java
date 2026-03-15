package edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepoUsuario extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}