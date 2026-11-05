package com.booktruck.model;

import com.booktruck.model.enums.TipoEvaluador;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "calificaciones", uniqueConstraints = @UniqueConstraint(columnNames = {"viaje_id", "evaluador_tipo"}))
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportador_id", nullable = false)
    private Transportador transportador;

    // Quién califica: EMPRESA (al transportador) o TRANSPORTADOR (a la empresa)
    @Enumerated(EnumType.STRING)
    @Column(name = "evaluador_tipo", nullable = false, length = 20)
    private TipoEvaluador evaluadorTipo;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private Integer puntuacion;

    @Column(length = 500)
    private String comentario;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
    }

    public Calificacion() {}

    // Getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Viaje getViaje() { return viaje; }
    public void setViaje(Viaje viaje) { this.viaje = viaje; }

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

    public Transportador getTransportador() { return transportador; }
    public void setTransportador(Transportador transportador) { this.transportador = transportador; }

    public TipoEvaluador getEvaluadorTipo() { return evaluadorTipo; }
    public void setEvaluadorTipo(TipoEvaluador evaluadorTipo) { this.evaluadorTipo = evaluadorTipo; }

    public Integer getPuntuacion() { return puntuacion; }
    public void setPuntuacion(Integer puntuacion) { this.puntuacion = puntuacion; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}