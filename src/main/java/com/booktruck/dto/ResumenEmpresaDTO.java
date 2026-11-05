package com.booktruck.dto;

import com.booktruck.model.Carga;

import java.util.List;

public class ResumenEmpresaDTO {

    private final long cargasActivas;
    private final long ofertasPendientes;
    private final long viajesEnCurso;
    private final List<Carga> cargasRecientes;

    public ResumenEmpresaDTO(long cargasActivas, long ofertasPendientes, long viajesEnCurso, List<Carga> cargasRecientes) {
        this.cargasActivas = cargasActivas;
        this.ofertasPendientes = ofertasPendientes;
        this.viajesEnCurso = viajesEnCurso;
        this.cargasRecientes = cargasRecientes;
    }

    public long getCargasActivas() { return cargasActivas; }
    public long getOfertasPendientes() { return ofertasPendientes; }
    public long getViajesEnCurso() { return viajesEnCurso; }
    public List<Carga> getCargasRecientes() { return cargasRecientes; }
}
