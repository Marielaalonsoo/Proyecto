package edu.comillas.icai.gitt.pat.spring.PistaPadel.Modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "usuarios")
public class Usuario {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer idUsuario;

        @Column(nullable = false)
        private String nombre;

        @Column(nullable = false)
        private String apellidos;

        @Column(nullable = false, unique = true) //único y no nulo
        private String email;

        @Column(nullable = false)
        private String passwordHash;

        @Column(nullable = false)
        private String telefono;

        @Column(nullable = false)
        private Rol rol;

        @Column(nullable = false)
        private LocalDateTime fechaRegistro;

        @Column(nullable = false)
        private boolean activo;

        @JsonIgnore
        @OneToMany(mappedBy = "usuario")
        private List<Reserva> reservas = new ArrayList<>();

    public Usuario() { }

    public Usuario(Integer idUsuario, String nombre, String apellidos, String email,
                   String passwordHash, String telefono, Rol rol,
                   LocalDateTime fechaRegistro, boolean activo) {

            this.idUsuario = idUsuario;
            this.nombre = nombre;
            this.apellidos = apellidos;
            this.email = email;
            this.passwordHash = passwordHash;
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

    public String getPasswordHash() {
        return passwordHash;
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

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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
