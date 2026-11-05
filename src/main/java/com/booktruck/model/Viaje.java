package com.booktruck.model;

import com.booktruck.model.enums.EstadoViaje;
import com.booktruck.model.enums.MotivoCancelacion;
import com.booktruck.model.enums.RolUsuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "viajes")
public class Viaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Carga, vehículo y transportador se copian de la oferta aceptada al crear el viaje
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id", nullable = false, unique = true)
    private Oferta oferta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carga_id", nullable = false)
    private Carga carga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportador_id", nullable = false)
    private Transportador transportador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoViaje estado = EstadoViaje.PROGRAMADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_cancelacion", length = 30)
    private MotivoCancelacion motivoCancelacion;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    // Comisión: se calcula al confirmar la recepción y se guarda el porcentaje usado
    @Column(name = "comision_porcentaje", precision = 5, scale = 2)
    private BigDecimal comisionPorcentaje;

    @Column(name = "comision_monto", precision = 12, scale = 2)
    private BigDecimal comisionMonto;

    // Quién canceló el viaje (null si no se canceló)
    @Enumerated(EnumType.STRING)
    @Column(name = "cancelado_por", length = 20)
    private RolUsuario canceladoPor;

    // Prueba de entrega (foto o remesa) que sube el transportador
    @Column(name = "evidencia_ruta", length = 255)
    private String evidenciaRuta;

    @Column(name = "evidencia_nombre_original", length = 255)
    private String evidenciaNombreOriginal;

    // Horas después de la recolección para marcar como atrasado un viaje sin iniciar
    public static final int HORAS_GRACIA_INICIO = 2;

    public Viaje() {}

    /** true si sigue programado y ya pasaron las horas de gracia */
    public boolean isAtrasado() {
        return estado == EstadoViaje.PROGRAMADO && carga != null && carga.getFechaRecoleccion() != null
                && carga.getFechaRecoleccion().plusHours(HORAS_GRACIA_INICIO).isBefore(LocalDateTime.now());
    }

    public boolean isTienePruebaEntrega() {
        return evidenciaRuta != null;
    }

    public RolUsuario getCanceladoPor() { return canceladoPor; }
    public void setCanceladoPor(RolUsuario canceladoPor) { this.canceladoPor = canceladoPor; }

    public String getEvidenciaRuta() { return evidenciaRuta; }
    public void setEvidenciaRuta(String evidenciaRuta) { this.evidenciaRuta = evidenciaRuta; }

    public String getEvidenciaNombreOriginal() { return evidenciaNombreOriginal; }
    public void setEvidenciaNombreOriginal(String evidenciaNombreOriginal) { this.evidenciaNombreOriginal = evidenciaNombreOriginal; }

    // Getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Oferta getOferta() { return oferta; }
    public void setOferta(Oferta oferta) { this.oferta = oferta; }

    public Carga getCarga() { return carga; }
    public void setCarga(Carga carga) { this.carga = carga; }

    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }

    public Transportador getTransportador() { return transportador; }
    public void setTransportador(Transportador transportador) { this.transportador = transportador; }

    public EstadoViaje getEstado() { return estado; }
    public void setEstado(EstadoViaje estado) { this.estado = estado; }

    public MotivoCancelacion getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(MotivoCancelacion motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public BigDecimal getComisionPorcentaje() { return comisionPorcentaje; }
    public void setComisionPorcentaje(BigDecimal comisionPorcentaje) { this.comisionPorcentaje = comisionPorcentaje; }

    public BigDecimal getComisionMonto() { return comisionMonto; }
    public void setComisionMonto(BigDecimal comisionMonto) { this.comisionMonto = comisionMonto; }

    // Lo que recibe el transportador una vez descontada la comisión
    public BigDecimal getNetoTransportador() {
        if (comisionMonto == null || oferta == null) {
            return null;
        }
        return oferta.getMonto().subtract(comisionMonto);
    }
}