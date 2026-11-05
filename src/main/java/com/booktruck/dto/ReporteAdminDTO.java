package com.booktruck.dto;

import com.booktruck.model.enums.EstadoCarga;
import com.booktruck.model.enums.EstadoViaje;

import java.math.BigDecimal;
import java.util.Map;

public class ReporteAdminDTO {

    private final long totalCargas;
    private final Map<EstadoCarga, Long> cargasPorEstado;
    private final BigDecimal sumaPeso;
    private final BigDecimal sumaPrecioOfrecido;

    private final long totalOfertas;
    private final BigDecimal montoPromedioOfertas;

    private final long totalViajesIniciados;
    private final Map<EstadoViaje, Long> viajesPorEstado;

    private final long totalViajesConComision;
    private final BigDecimal valorTransportado;
    private final BigDecimal totalComisiones;

    private final long totalCalificaciones;
    private final Double promedioCalificacionEmpresa;
    private final Double promedioCalificacionTransportador;

    public ReporteAdminDTO(long totalCargas, Map<EstadoCarga, Long> cargasPorEstado, BigDecimal sumaPeso,
                            BigDecimal sumaPrecioOfrecido, long totalOfertas, BigDecimal montoPromedioOfertas,
                            long totalViajesIniciados, Map<EstadoViaje, Long> viajesPorEstado,
                            long totalViajesConComision, BigDecimal valorTransportado, BigDecimal totalComisiones,
                            long totalCalificaciones, Double promedioCalificacionEmpresa,
                            Double promedioCalificacionTransportador) {
        this.totalCargas = totalCargas;
        this.cargasPorEstado = cargasPorEstado;
        this.sumaPeso = sumaPeso;
        this.sumaPrecioOfrecido = sumaPrecioOfrecido;
        this.totalOfertas = totalOfertas;
        this.montoPromedioOfertas = montoPromedioOfertas;
        this.totalViajesIniciados = totalViajesIniciados;
        this.viajesPorEstado = viajesPorEstado;
        this.totalViajesConComision = totalViajesConComision;
        this.valorTransportado = valorTransportado;
        this.totalComisiones = totalComisiones;
        this.totalCalificaciones = totalCalificaciones;
        this.promedioCalificacionEmpresa = promedioCalificacionEmpresa;
        this.promedioCalificacionTransportador = promedioCalificacionTransportador;
    }

    public long getTotalCargas() { return totalCargas; }
    public Map<EstadoCarga, Long> getCargasPorEstado() { return cargasPorEstado; }
    public BigDecimal getSumaPeso() { return sumaPeso; }
    public BigDecimal getSumaPrecioOfrecido() { return sumaPrecioOfrecido; }
    public long getTotalOfertas() { return totalOfertas; }
    public BigDecimal getMontoPromedioOfertas() { return montoPromedioOfertas; }
    public long getTotalViajesIniciados() { return totalViajesIniciados; }
    public Map<EstadoViaje, Long> getViajesPorEstado() { return viajesPorEstado; }
    public long getTotalViajesConComision() { return totalViajesConComision; }
    public BigDecimal getValorTransportado() { return valorTransportado; }
    public BigDecimal getTotalComisiones() { return totalComisiones; }
    public long getTotalCalificaciones() { return totalCalificaciones; }
    public Double getPromedioCalificacionEmpresa() { return promedioCalificacionEmpresa; }
    public Double getPromedioCalificacionTransportador() { return promedioCalificacionTransportador; }
}
