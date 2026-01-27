package com.faguaslandia.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "juegos")
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo")
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
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }


    public String getDescripcion() { return descripcion; }

    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagen_url() { return imagen_url; }

    public void setImagen_url(String imagen_url) { this.imagen_url = imagen_url; }

    public String getCategoria() { return categoria; }

    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDesarrollador() { return desarrollador; }

    public void setDesarrollador(String desarrollador) { this.desarrollador = desarrollador; }

    public LocalDate getFecha_lanzamiento() { return fecha_lanzamiento; }

    public void setFecha_lanzamiento(LocalDate fecha_lanzamiento) { this.fecha_lanzamiento = fecha_lanzamiento; }

    public BigDecimal getValoracion_promedio() { return valoracion_promedio; }

    public void setValoracion_promedio(BigDecimal valoracion_promedio) { this.valoracion_promedio = valoracion_promedio; }
}
