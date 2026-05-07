package com.faguaslandia.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(name = "avatar_url")
    private String foto;

    @Enumerated(EnumType.STRING)
    private EstadoUsuario estado = EstadoUsuario.offline;

    // Última vez que el frontend hizo ping (heartbeat)
    @Column(name = "ultima_actividad")
    private LocalDateTime ultimaActividad;

    public Usuario() {}

    public Usuario(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.estado = EstadoUsuario.offline;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public EstadoUsuario getEstado() { return estado; }
    public void setEstado(EstadoUsuario estado) { this.estado = estado; }

    public LocalDateTime getUltimaActividad() { return ultimaActividad; }
    public void setUltimaActividad(LocalDateTime ultimaActividad) { this.ultimaActividad = ultimaActividad; }
}