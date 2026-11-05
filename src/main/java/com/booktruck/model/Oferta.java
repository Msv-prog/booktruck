package com.booktruck.model;

import com.booktruck.model.enums.EstadoOferta;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ofertas", uniqueConstraints = @UniqueConstraint(columnNames = {"carga_id", "transportador_id"}))
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carga_id", nullable = false)
    private Carga carga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportador_id", nullable = false)
    private Transportador transportador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOferta estado = EstadoOferta.PENDIENTE;

    // Se actualiza cuando el transportador vuelve a ofertar (se reutiliza la misma fila)
    @Column(name = "fecha_oferta", nullable = false)
    private LocalDateTime fechaOferta;

    @PrePersist
    protected void onCreate() {
        this.fechaOferta = LocalDateTime.now();
    }

    public Oferta() {}

    // Getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Carga getCarga() { return carga; }
    public void setCarga(Carga carga) { this.carga = carga; }

    public Transportador getTransportador() { return transportador; }
    public void setTransportador(Transportador transportador) { this.transportador = transportador; }

    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public EstadoOferta getEstado() { return estado; }
    public void setEstado(EstadoOferta estado) { this.estado = estado; }

    public LocalDateTime getFechaOferta() { return fechaOferta; }
    public void setFechaOferta(LocalDateTime fechaOferta) { this.fechaOferta = fechaOferta; }
}