package com.faguaslandia.dto;

public class MensajeRequest {

    private Long emisorId;
    private Long receptorId;
    private String contenido;

    public MensajeRequest() {}

    public Long getEmisorId() { return emisorId; }
    public void setEmisorId(Long emisorId) { this.emisorId = emisorId; }

    public Long getReceptorId() { return receptorId; }
    public void setReceptorId(Long receptorId) { this.receptorId = receptorId; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
}