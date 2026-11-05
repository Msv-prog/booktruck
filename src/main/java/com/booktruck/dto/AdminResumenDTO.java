package com.booktruck.dto;

public class AdminResumenDTO {

    private final long totalUsuarios;
    private final long totalEmpresas;
    private final long totalTransportadores;
    private final long totalVehiculos;
    private final long totalCargas;
    private final long cargasActivas;
    private final long totalOfertas;
    private final long totalViajes;
    private final long viajesEnCurso;
    private final long totalCalificaciones;

    public AdminResumenDTO(long totalUsuarios, long totalEmpresas, long totalTransportadores, long totalVehiculos,
                            long totalCargas, long cargasActivas, long totalOfertas, long totalViajes,
                            long viajesEnCurso, long totalCalificaciones) {
        this.totalUsuarios = totalUsuarios;
        this.totalEmpresas = totalEmpresas;
        this.totalTransportadores = totalTransportadores;
        this.totalVehiculos = totalVehiculos;
        this.totalCargas = totalCargas;
        this.cargasActivas = cargasActivas;
        this.totalOfertas = totalOfertas;
        this.totalViajes = totalViajes;
        this.viajesEnCurso = viajesEnCurso;
        this.totalCalificaciones = totalCalificaciones;
    }

    public long getTotalUsuarios() { return totalUsuarios; }
    public long getTotalEmpresas() { return totalEmpresas; }
    public long getTotalTransportadores() { return totalTransportadores; }
    public long getTotalVehiculos() { return totalVehiculos; }
    public long getTotalCargas() { return totalCargas; }
    public long getCargasActivas() { return cargasActivas; }
    public long getTotalOfertas() { return totalOfertas; }
    public long getTotalViajes() { return totalViajes; }
    public long getViajesEnCurso() { return viajesEnCurso; }
    public long getTotalCalificaciones() { return totalCalificaciones; }
}
