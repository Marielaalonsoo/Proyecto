package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import java.time.LocalDate;
import java.time.LocalTime;

public record ModeloReserva(
        Integer courtId,
        LocalDate date,
        LocalTime time,
        Integer durationMinutes
) {}