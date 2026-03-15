package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import java.time.LocalDate;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pistas")
public class Pista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPista;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private int precioHora;

    @Column(nullable = false)
    private String ubicacion;

    @Column(nullable = false)
    private boolean activa = true;

    @Column(nullable = false)
    private LocalDate fechaAlta;

    @OneToMany(mappedBy = "pista")
    private List<Reserva> reservas = new ArrayList<>();

    public Pista() { }

    public Pista(Integer idPista, String nombre, String ubicacion, int precioHora, boolean activa, LocalDate fechaAlta) {
        this.idPista = idPista;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.precioHora = precioHora;
        this.activa = activa;
        this.fechaAlta = fechaAlta;
    }

    public Integer getIdPista() { return idPista; }
    public String getNombre() { return nombre; }
    public int getPrecioHora() { return precioHora; }
    public String getUbicacion() { return ubicacion; }
    public boolean isActiva() { return activa; }
    public LocalDate getFechaAlta() { return fechaAlta; }

    public void setIdPista(Integer idPista) { this.idPista = idPista; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPrecioHora(int precioHora) { this.precioHora = precioHora; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public void setActiva(boolean activa) { this.activa = activa; }
    public void setFechaAlta(LocalDate fechaAlta) { this.fechaAlta = fechaAlta; }
}
