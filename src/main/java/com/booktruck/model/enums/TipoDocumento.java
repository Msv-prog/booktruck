package com.booktruck.model.enums;

import java.util.Arrays;
import java.util.List;

/** Documentos que se piden a cada tipo de dueño (deben coincidir con chk_documentos_tipo). */
public enum TipoDocumento {

    RUT(DuenoDocumento.EMPRESA, "RUT", false, true, false,
            "Registro Único Tributario expedido por la DIAN."),
    CAMARA_COMERCIO(DuenoDocumento.EMPRESA, "Certificado de Cámara de Comercio", false, true, false,
            "Certificado de existencia y representación legal, con menos de 30 días de expedido."),
    CEDULA_REPRESENTANTE(DuenoDocumento.EMPRESA, "Cédula del representante legal", true, true, false,
            "Ambas caras de la cédula del representante legal."),

    CEDULA(DuenoDocumento.TRANSPORTADOR, "Cédula de ciudadanía", true, true, false,
            "Ambas caras de tu cédula."),
    LICENCIA_CONDUCCION(DuenoDocumento.TRANSPORTADOR, "Licencia de conducción", true, true, true,
            "Licencia vigente de la categoría que registraste (C1 hasta 3.500 kg, C2 hasta 10.000 kg, C3 articulados y más pesados)."),

    LICENCIA_TRANSITO(DuenoDocumento.VEHICULO, "Licencia de tránsito", true, true, false,
            "Tarjeta de propiedad del vehículo."),
    SOAT(DuenoDocumento.VEHICULO, "SOAT", false, true, true,
            "Seguro obligatorio vigente."),
    TECNICOMECANICA(DuenoDocumento.VEHICULO, "Revisión técnico-mecánica", true, true, true,
            "Certificado de revisión técnico-mecánica y de emisiones vigente.");

    private final DuenoDocumento dueno;
    private final String etiqueta;
    private final boolean femenino;
    private final boolean obligatorio;
    private final boolean vence;
    private final String descripcion;

    TipoDocumento(DuenoDocumento dueno, String etiqueta, boolean femenino, boolean obligatorio, boolean vence,
                  String descripcion) {
        this.dueno = dueno;
        this.etiqueta = etiqueta;
        this.femenino = femenino;
        this.obligatorio = obligatorio;
        this.vence = vence;
        this.descripcion = descripcion;
    }

    /** concordar("rechazad") devuelve "rechazada" o "rechazado" según el documento */
    public String concordar(String raiz) {
        return raiz + (femenino ? "a" : "o");
    }

    public DuenoDocumento getDueno() { return dueno; }
    public String getEtiqueta() { return etiqueta; }
    public boolean isObligatorio() { return obligatorio; }
    /** Si el documento tiene fecha de vencimiento (obligatoria al subirlo). */
    public boolean isVence() { return vence; }
    public String getDescripcion() { return descripcion; }

    public static List<TipoDocumento> de(DuenoDocumento dueno) {
        return Arrays.stream(values()).filter(t -> t.dueno == dueno).toList();
    }
}
