package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import java.time.LocalDate;

public class Pista {

    private int idPista;
    private String nombre ;
    private String ubicacion;
    private boolean activa = true;
    private LocalDate fechaAta;

    public Pista(int idPista, String nombre, String ubicacion, boolean activa,LocalDate fechaAta) {
        this.idPista = this.idPista;
        this.nombre = this.nombre;
        this.ubicacion = this.ubicacion;
        this.activa = this.activa;
        this.fechaAta = this.fechaAta;
    }

    public int getIdPista() {
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
        return fechaAta;
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
        this.fechaAta = fechaAta;
    }

}
