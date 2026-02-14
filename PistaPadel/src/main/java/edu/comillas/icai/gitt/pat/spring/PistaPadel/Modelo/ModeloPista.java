package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;
import jakarta.validation.constraints.NotBlank;
public record ModeloPista (
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    @NotBlank(message = "La ubicación es obligatoria")
    String ubicacion,

    // Para PATCH: si no lo envían, queda null y no lo modificas
    Boolean activa

) { }
