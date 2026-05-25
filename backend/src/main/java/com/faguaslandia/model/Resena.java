package com.faguaslandia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resenas", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_usuario", "id_juego"})
})
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_juego", nullable = false)
    private Juego juego;

    @Column(nullable = false)
    private int puntuacion;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    public Resena() {}

    public Resena(Usuario usuario, Juego juego, int puntuacion, String comentario) {
        this.usuario    = usuario;
        this.juego      = juego;
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.fecha      = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Juego getJuego() { return juego; }
    public void setJuego(Juego juego) { this.juego = juego; }

    public int getPuntuacion() { return puntuacion; }
    public void setPuntuacion(int puntuacion) { this.puntuacion = puntuacion; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}