package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

public record ModeloReserva(
        @NotNull Integer courtId,
        @NotNull LocalDate date,
        @NotNull LocalTime time,
        @NotNull @Positive Integer durationMinutes
) {}
