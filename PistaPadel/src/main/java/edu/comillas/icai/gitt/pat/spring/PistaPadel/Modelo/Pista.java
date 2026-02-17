package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import java.time.LocalDate;

public class Pista {

    private Integer idPista;
    private String nombre;
    private int precioHora;
    private String ubicacion;
    private boolean activa = true;
    private LocalDate fechaAlta;

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
