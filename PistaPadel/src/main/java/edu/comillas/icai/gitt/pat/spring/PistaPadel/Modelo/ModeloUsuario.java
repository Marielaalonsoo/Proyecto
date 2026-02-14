package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ModeloUsuario(
        @NotBlank String nombre,
        String apellidos,
        @NotBlank @Email String email,
        @NotBlank String password,
        String telefono
) {}

