package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import java.time.LocalDateTime;

public class Usuario {

        private Integer idUsuario;
        private String nombre;
        private String apellidos;
        private String email;
        private String password;
        private String telefono;
        private Rol rol;
        private LocalDateTime fechaRegistro;
        private boolean activo;

    public Usuario() { }

    public Usuario(Integer idUsuario, String nombre, String apellidos, String email,
                       String password, String telefono, Rol rol,
                       LocalDateTime fechaRegistro, boolean activo) {

            this.idUsuario = idUsuario;
            this.nombre = nombre;
            this.apellidos = apellidos;
            this.email = email;
            this.password = password;
            this.telefono = telefono;
            this.rol = rol;
            this.fechaRegistro = fechaRegistro;
            this.activo = activo;
        }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getTelefono() {
        return telefono;
    }

    public Rol getRol() {
        return rol;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
