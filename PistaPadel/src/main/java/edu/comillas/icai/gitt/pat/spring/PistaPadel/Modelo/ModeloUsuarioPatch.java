package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import jakarta.validation.constraints.Email;

public record ModeloUsuarioPatch(
        String nombre,
        String apellidos,
        @Email(message = "email no válido") String email,
        String telefono
) {}
