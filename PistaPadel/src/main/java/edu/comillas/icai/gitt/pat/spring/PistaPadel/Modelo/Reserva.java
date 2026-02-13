package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Reserva {


    private Integer idReserva;
    private Integer idUsuario ;
    private Integer idPista ;
    private LocalDate fechaReserva;
    private LocalTime horaInicio;
    private LocalTime horaFin;;
    private int duracionMinutosa;
    private String estado;
    private LocalDateTime fechaCreacion;

    public Reserva(Integer idPista, String nombre, String ubicacion, boolean activa,LocalDate fechaAta) {
        this.idReserva = this.idReserva;
        this.idUsuario = this.idUsuario;
        this.idPista = this.idPista;
        this.fechaReserva = this.fechaReserva;
        this.horaInicio = this.horaInicio;
        this.horaFin = this.horaFin;
        this.duracionMinutosa = this.duracionMinutosa;
        this.estado = this.estado;
        this.fechaCreacion = this.fechaCreacion;

    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public Integer getIdReserva() {
        return idReserva;
    }

    public LocalDate getFechaReserva() {
        return fechaReserva;
    }

    public Integer getIdPista() {
        return idPista;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public int getDuracionMinutosa() {
        return duracionMinutosa;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setIdReserva(Integer idReserva) {
        this.idReserva = idReserva;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setFechaReserva(LocalDate fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public void setDuracionMinutosa(int duracionMinutosa) {
        this.duracionMinutosa = duracionMinutosa;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public void setIdPista(Integer idPista) {
        this.idPista = idPista;
    }
}
