package com.booktruck.dto;

public class ResumenCalificacion {

    private final Double promedio;
    private final Long cantidad;

    public ResumenCalificacion(Double promedio, Long cantidad) {
        this.promedio = promedio;
        this.cantidad = cantidad;
    }

    public Double getPromedio() { return promedio; }
    public Long getCantidad() { return cantidad; }
}
