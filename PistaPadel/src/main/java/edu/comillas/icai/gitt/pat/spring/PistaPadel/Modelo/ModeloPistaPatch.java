package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import java.time.LocalDate;

public record ModeloPistaPatch(
        String nombre,
        String ubicacion,
        Integer precioHora,
        Boolean activa,
        LocalDate fechaAlta
) { }
