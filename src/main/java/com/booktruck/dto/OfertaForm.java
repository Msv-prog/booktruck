package com.booktruck.dto;

import java.math.BigDecimal;

public class OfertaForm {

    private BigDecimal monto;
    private Long vehiculoId;

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }
}
