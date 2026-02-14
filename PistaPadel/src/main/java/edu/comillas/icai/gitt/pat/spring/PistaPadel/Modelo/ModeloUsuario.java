package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ModeloUsuario(
        @NotBlank(message = "nombre obligatorio") String nombre,
        String apellidos,
        @NotBlank(message = "email obligatorio") @Email(message = "email no válido") String email,
        @NotBlank(message = "password obligatorio") String password,
        String telefono
) { }
