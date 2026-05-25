package com.faguaslandia.dto;

public class ResenaRequest {
    private int puntuacion;
    private String comentario;

    public ResenaRequest() {}

    public int getPuntuacion() { return puntuacion; }
    public void setPuntuacion(int puntuacion) { this.puntuacion = puntuacion; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}