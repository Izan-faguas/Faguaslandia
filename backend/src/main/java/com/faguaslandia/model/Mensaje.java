package com.faguaslandia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_emisor")
    private Usuario emisor;

    @ManyToOne
    @JoinColumn(name = "id_receptor")
    private Usuario receptor;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "leido")
    private boolean leido = false;

    public Mensaje() {}

    public Mensaje(Usuario emisor, Usuario receptor, String contenido) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenido = contenido;
        this.fechaEnvio = LocalDateTime.now();
        this.leido = false;
    }

    public Long getId() { return id; }

    public Usuario getEmisor() { return emisor; }
    public void setEmisor(Usuario emisor) { this.emisor = emisor; }

    public Usuario getReceptor() { return receptor; }
    public void setReceptor(Usuario receptor) { this.receptor = receptor; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }
}