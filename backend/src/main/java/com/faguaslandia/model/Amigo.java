package com.faguaslandia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "amigos")
public class Amigo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario1")
    private Usuario usuario1;

    @ManyToOne
    @JoinColumn(name = "id_usuario2")
    private Usuario usuario2;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('pendiente','aceptado','bloqueado')")
    private EstadoAmigo estado = EstadoAmigo.pendiente;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    public Amigo() {}

    public Amigo(Usuario usuario1, Usuario usuario2) {
        this.usuario1 = usuario1;
        this.usuario2 = usuario2;
        this.estado = EstadoAmigo.pendiente;
        this.fechaSolicitud = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Usuario getUsuario1() { return usuario1; }
    public void setUsuario1(Usuario usuario1) { this.usuario1 = usuario1; }

    public Usuario getUsuario2() { return usuario2; }
    public void setUsuario2(Usuario usuario2) { this.usuario2 = usuario2; }

    public EstadoAmigo getEstado() { return estado; }
    public void setEstado(EstadoAmigo estado) { this.estado = estado; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }
}