package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import jakarta.validation.constraints.Email;

public record ModeloUsuario(
        String nombre,
        String apellidos,
        @Email(message = "email no válido") String email,
        String password,
        String telefono
) {}
