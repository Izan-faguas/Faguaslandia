package com.faguaslandia.launcher.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)   // ← ignora campos extra del backend
public class Usuario {

    private Long id;
    private String nombre;
    private String email;
    private String password;
    private String foto;
    private String estado;

    public Usuario() {}

    public Usuario(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }

    // Getters y setters existentes
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

    public String getEstado() { return estado; }       // ← nuevo
    public void setEstado(String estado) { this.estado = estado; }  // ← nuevo
}