package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.*;

@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idReserva;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pista_id", nullable = false)
    private Pista pista;

    //private Integer idReserva;
    //private Integer idUsuario;
    //private Integer idPista;

    @Column(nullable = false)
    private LocalDate fechaReserva;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private int duracionMinutos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    public Reserva () {}

    public Reserva(Integer idReserva, Usuario usuario, Pista pista,
                   LocalDate fechaReserva, LocalTime horaInicio,
                   int duracionMinutos, EstadoReserva estado,
                   LocalDateTime fechaCreacion) {
        this.idReserva = idReserva;
        this.usuario = usuario;
        this.pista = pista;
        this.fechaReserva = fechaReserva;
        this.horaInicio = horaInicio;
        this.duracionMinutos = duracionMinutos;
        this.horaFin = (horaInicio != null) ? horaInicio.plusMinutes(duracionMinutos) : null;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Pista getPista() { return pista; }
    public void setPista(Pista pista) { this.pista = pista; }

    public LocalDate getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(LocalDate fechaReserva) { this.fechaReserva = fechaReserva; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
        this.horaFin = (horaInicio != null) ? horaInicio.plusMinutes(this.duracionMinutos) : null;
    }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public int getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
        this.horaFin = (this.horaInicio != null) ? this.horaInicio.plusMinutes(duracionMinutos) : null;
    }

    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
