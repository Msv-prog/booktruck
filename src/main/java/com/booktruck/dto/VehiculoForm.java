package com.booktruck.dto;

import com.booktruck.model.enums.TipoCarroceria;
import com.booktruck.model.enums.TipoVehiculo;

import java.math.BigDecimal;

public class VehiculoForm {

    private String placa;
    private TipoVehiculo tipoVehiculo;
    private String marca;
    private BigDecimal capacidadToneladas;
    private TipoCarroceria tipoCarroceria;
    private Integer pesoBrutoKg;

    public Integer getPesoBrutoKg() { return pesoBrutoKg; }
    public void setPesoBrutoKg(Integer pesoBrutoKg) { this.pesoBrutoKg = pesoBrutoKg; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public TipoVehiculo getTipoVehiculo() { return tipoVehiculo; }
    public void setTipoVehiculo(TipoVehiculo tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public BigDecimal getCapacidadToneladas() { return capacidadToneladas; }
    public void setCapacidadToneladas(BigDecimal capacidadToneladas) { this.capacidadToneladas = capacidadToneladas; }

    public TipoCarroceria getTipoCarroceria() { return tipoCarroceria; }
    public void setTipoCarroceria(TipoCarroceria tipoCarroceria) { this.tipoCarroceria = tipoCarroceria; }
}
