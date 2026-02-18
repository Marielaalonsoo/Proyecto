package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public record ModeloPistaCrear(
        @NotBlank String nombre,
        @NotBlank String ubicacion,
        @NotNull @PositiveOrZero Integer precioHora,
        @NotNull Boolean activa,
        @NotNull LocalDate fechaAlta
) {}