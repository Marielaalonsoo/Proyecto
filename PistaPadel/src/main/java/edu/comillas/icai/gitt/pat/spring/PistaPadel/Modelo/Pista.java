package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import java.time.LocalDate;

public class Pista {

    private Integer idPista;
    private String nombre ;
    private int precioHora;
    private String ubicacion;
    private boolean activa = true;
    private LocalDate fechaAlta;

    public Pista(Integer idPista, String nombre, String ubicacion, boolean activa,LocalDate fechaAta) {
        this.idPista = idPista;
        this.nombre = nombre;
        this.precioHora = precioHora;
        this.ubicacion = ubicacion;
        this.activa = activa;
        this.fechaAlta = fechaAta;
    }

    public Integer getIdPista() {
        return idPista;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public boolean isActiva() {
        return activa;
    }

    public LocalDate getFechaAta() {
        return fechaAlta;
    }

    public void setIdPista(int idPista) {
        this.idPista = idPista;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public void setFechaAta(LocalDate fechaAta) {
        this.fechaAlta = fechaAta;
    }

    public int getPrecioHora() { return precioHora; }
    public void setPrecioHora(int precioHora) { this.precioHora = precioHora; }

}
