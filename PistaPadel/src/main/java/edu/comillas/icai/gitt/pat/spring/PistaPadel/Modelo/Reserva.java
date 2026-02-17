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
    private EstadoReserva estado;
    private LocalDateTime fechaCreacion;

    public Reserva(Integer idReserva, Integer idUsuario, Integer idPista,
                   LocalDate fechaReserva, LocalTime horaInicio,
                   int duracionMinutos, EstadoReserva estado,
                   LocalDateTime fechaCreacion) {
        // corregido,  Asignar el parámetro (derecha) al atributo (izquierda)
        this.idReserva = idReserva;
        this.idUsuario = idUsuario;
        this.idPista = idPista;
        this.fechaReserva = fechaReserva;
        this.horaInicio = horaInicio;
        // calcula la horaFin automáticamente
        this.duracionMinutosa = duracionMinutos;
        this.horaFin = horaInicio.plusMinutes(duracionMinutos);
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
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

    public EstadoReserva getEstado() {
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

    public void setEstado(EstadoReserva estado) {
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
