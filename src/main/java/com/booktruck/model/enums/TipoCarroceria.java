package com.booktruck.model.enums;

public enum TipoCarroceria {
    ESTACAS("Estacas"),
    FURGON("Furgón"),
    PLANCHA("Plancha"),
    CONTENEDOR("Contenedor"),
    REFRIGERADO("Refrigerado"),
    TOLVA("Tolva");

    private final String etiqueta;

    TipoCarroceria(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() { return etiqueta; }
}
