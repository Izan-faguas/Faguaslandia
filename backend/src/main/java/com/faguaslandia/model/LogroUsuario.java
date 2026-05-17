package com.faguaslandia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "logros_usuarios")
public class LogroUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_logro")
    private Logro logro;

    @Column(name = "fecha_desbloqueo")
    private LocalDateTime fechaDesbloqueo;

    @Column(name = "notificado")
    private boolean notificado = false;

    public LogroUsuario() {}

    public Long getId() { return id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Logro getLogro() { return logro; }
    public void setLogro(Logro logro) { this.logro = logro; }

    public LocalDateTime getFechaDesbloqueo() { return fechaDesbloqueo; }
    public void setFechaDesbloqueo(LocalDateTime fechaDesbloqueo) { this.fechaDesbloqueo = fechaDesbloqueo; }

    public boolean isNotificado() { return notificado; }
    public void setNotificado(boolean notificado) { this.notificado = notificado; }
}