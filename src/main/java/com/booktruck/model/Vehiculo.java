package com.booktruck.model;

import com.booktruck.model.enums.CategoriaLicencia;
import com.booktruck.model.enums.TipoCarroceria;
import com.booktruck.model.enums.TipoVehiculo;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "vehiculos")
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportador_id", nullable = false)
    private Transportador transportador;

    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo", nullable = false, length = 20)
    private TipoVehiculo tipoVehiculo = TipoVehiculo.SENCILLO;

    @Column(nullable = false, length = 60)
    private String marca;

    @Column(name = "capacidad_toneladas", nullable = false, precision = 6, scale = 2)
    private BigDecimal capacidadToneladas;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_carroceria", nullable = false, length = 30)
    private TipoCarroceria tipoCarroceria;

    @Column(nullable = false)
    private Boolean activo = true;

    // Peso bruto vehicular en kg (vehículo vacío + carga máxima)
    @Column(name = "peso_bruto_kg")
    private Integer pesoBrutoKg;

    public Vehiculo() {}

    /** Licencia mínima para conducirlo; null si es sencillo y le falta el peso bruto. */
    public CategoriaLicencia getLicenciaRequerida() {
        return CategoriaLicencia.requeridaPara(tipoVehiculo, pesoBrutoKg);
    }

    /** Clase según el peso: Liviano (C1), Mediano (C2) o Pesado (C3). */
    public String getClasePeso() {
        CategoriaLicencia requerida = getLicenciaRequerida();
        if (requerida == null) {
            return null;
        }
        return switch (requerida) {
            case C1 -> "Liviano";
            case C2 -> "Mediano";
            case C3 -> "Pesado";
        };
    }

    // Getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Transportador getTransportador() { return transportador; }
    public void setTransportador(Transportador transportador) { this.transportador = transportador; }

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

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Integer getPesoBrutoKg() { return pesoBrutoKg; }
    public void setPesoBrutoKg(Integer pesoBrutoKg) { this.pesoBrutoKg = pesoBrutoKg; }
}