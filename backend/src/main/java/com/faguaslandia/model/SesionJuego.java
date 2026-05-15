package com.faguaslandia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones_juego")
public class SesionJuego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_juego")
    private Juego juego;

    @Column(name = "inicio")
    private LocalDateTime inicio;

    @Column(name = "fin")
    private LocalDateTime fin;

    @Column(name = "horas_jugadas", columnDefinition = "DECIMAL(10,2)")
    private Double horasJugadas = 0.0;

    public SesionJuego() {}

    public SesionJuego(Usuario usuario, Juego juego) {
        this.usuario = usuario;
        this.juego = juego;
        this.inicio = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Juego getJuego() { return juego; }
    public void setJuego(Juego juego) { this.juego = juego; }

    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }

    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) { this.fin = fin; }

    public Double getHorasJugadas() { return horasJugadas; }
    public void setHorasJugadas(Double horasJugadas) { this.horasJugadas = horasJugadas; }
}