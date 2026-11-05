package com.booktruck.dto;

public class ResumenTransportadorDTO {

    private final long vehiculosActivos;
    private final long ofertasPendientes;
    private final long viajesEnCurso;

    public ResumenTransportadorDTO(long vehiculosActivos, long ofertasPendientes, long viajesEnCurso) {
        this.vehiculosActivos = vehiculosActivos;
        this.ofertasPendientes = ofertasPendientes;
        this.viajesEnCurso = viajesEnCurso;
    }

    public long getVehiculosActivos() { return vehiculosActivos; }
    public long getOfertasPendientes() { return ofertasPendientes; }
    public long getViajesEnCurso() { return viajesEnCurso; }
}
