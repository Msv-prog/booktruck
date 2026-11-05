package com.booktruck.model;

import com.booktruck.model.enums.EstadoVerificacion;
import com.booktruck.model.enums.TipoDocumento;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Documento de verificación de una empresa, un transportador o un vehículo (uno por tipo). */
@Entity
@Table(name = "documentos")
public class Documento {

    /** Días antes del vencimiento en que se empieza a avisar. */
    public static final int DIAS_AVISO_VENCIMIENTO = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportador_id")
    private Transportador transportador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoDocumento tipo;

    @Column(name = "archivo_ruta", nullable = false, length = 255)
    private String archivoRuta;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVerificacion estado = EstadoVerificacion.PENDIENTE;

    @Column(name = "motivo_rechazo", length = 255)
    private String motivoRechazo;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    public Documento() {}

    // Reglas de vigencia (se evalúan con la fecha de hoy, no se guardan)

    public boolean isVencido() {
        return fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
    }

    /** Vigente pero vence dentro de los próximos DIAS_AVISO_VENCIMIENTO días. */
    public boolean isPorVencer() {
        return fechaVencimiento != null && !isVencido()
                && getDiasParaVencer() <= DIAS_AVISO_VENCIMIENTO;
    }

    public Long getDiasParaVencer() {
        return fechaVencimiento == null ? null : ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }

    /** Aprobado y sin vencer */
    public boolean isVigente() {
        return estado == EstadoVerificacion.APROBADO && !isVencido();
    }

    // Getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

    public Transportador getTransportador() { return transportador; }
    public void setTransportador(Transportador transportador) { this.transportador = transportador; }

    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }

    public TipoDocumento getTipo() { return tipo; }
    public void setTipo(TipoDocumento tipo) { this.tipo = tipo; }

    public String getArchivoRuta() { return archivoRuta; }
    public void setArchivoRuta(String archivoRuta) { this.archivoRuta = archivoRuta; }

    public String getNombreOriginal() { return nombreOriginal; }
    public void setNombreOriginal(String nombreOriginal) { this.nombreOriginal = nombreOriginal; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public EstadoVerificacion getEstado() { return estado; }
    public void setEstado(EstadoVerificacion estado) { this.estado = estado; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }

    public LocalDateTime getFechaRevision() { return fechaRevision; }
    public void setFechaRevision(LocalDateTime fechaRevision) { this.fechaRevision = fechaRevision; }
}
