package com.faguaslandia.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "juegos")
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private BigDecimal precio;

    private String imagen_url;

    private String categoria;

    private String desarrollador;

    private LocalDate fecha_lanzamiento;

    private BigDecimal valoracion_promedio;

    // --- GETTERS Y SETTERS ---
    public Integer getId() { return id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
}
