package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Reserva {

    private Integer idReserva;
    private Integer idUsuario;
    private Integer idPista;
    private LocalDate fechaReserva;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int duracionMinutos;
    private EstadoReserva estado;
    private LocalDateTime fechaCreacion;

    public Reserva(Integer idReserva, Integer idUsuario, Integer idPista,
                   LocalDate fechaReserva, LocalTime horaInicio,
                   int duracionMinutos, EstadoReserva estado,
                   LocalDateTime fechaCreacion) {

        this.idReserva = idReserva;
        this.idUsuario = idUsuario;
        this.idPista = idPista;
        this.fechaReserva = fechaReserva;
        this.horaInicio = horaInicio;
        this.duracionMinutos = duracionMinutos;
        this.horaFin = (horaInicio != null) ? horaInicio.plusMinutes(duracionMinutos) : null;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getIdReserva() { return idReserva; }
    public Integer getIdUsuario() { return idUsuario; }
    public Integer getIdPista() { return idPista; }
    public LocalDate getFechaReserva() { return fechaReserva; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public EstadoReserva getEstado() { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    public void setEstado(EstadoReserva estado) { this.estado = estado; }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
        this.horaFin = (horaInicio != null) ? horaInicio.plusMinutes(this.duracionMinutos) : null;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
        this.horaFin = (this.horaInicio != null) ? this.horaInicio.plusMinutes(duracionMinutos) : null;
    }
}
