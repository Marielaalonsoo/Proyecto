package edu.comillas.icai.gitt.pat.spring.PistaPadel.Repositorio;

import edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo.Usuario;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RepoUsuario extends CrudRepository<Usuario, Integer> {
    Optional<Usuario> findByEmailIgnoreCase(String email); //busca usuario por su correo electrónico; si el usuario no existe, devuelve vacío
    boolean existsByEmailIgnoreCase(String email); //consulta rápida para ver si existe el email
}