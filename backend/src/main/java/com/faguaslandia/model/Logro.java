package com.faguaslandia.model;

import jakarta.persistence.*;

@Entity
@Table(name = "logros")
public class Logro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;

    @Column(name = "icono_url")
    private String iconoUrl;

    @Enumerated(EnumType.STRING)
    private TipoLogro tipo;

    @ManyToOne
    @JoinColumn(name = "id_juego")
    private Juego juego;

    public Logro() {}

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getIconoUrl() { return iconoUrl; }
    public void setIconoUrl(String iconoUrl) { this.iconoUrl = iconoUrl; }
    public TipoLogro getTipo() { return tipo; }
    public void setTipo(TipoLogro tipo) { this.tipo = tipo; }
    public Juego getJuego() { return juego; }
    public void setJuego(Juego juego) { this.juego = juego; }
}