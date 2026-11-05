package com.booktruck.model;

import com.booktruck.model.enums.EstadoCarga;
import com.booktruck.model.enums.TipoCarroceria;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cargas")
public class Carga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 100)
    private String origen;

    @Column(nullable = false, length = 100)
    private String destino;

    @Column(name = "direccion_origen", nullable = false, length = 200)
    private String direccionOrigen;

    @Column(name = "direccion_destino", nullable = false, length = 200)
    private String direccionDestino;

    @Column(name = "tipo_mercancia", nullable = false, length = 100)
    private String tipoMercancia;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal peso;

    @Column(name = "precio_ofrecido", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioOfrecido;

    @Column(name = "fecha_recoleccion", nullable = false)
    private LocalDateTime fechaRecoleccion;

    @Column(name = "fecha_publicacion", nullable = false, updatable = false)
    private LocalDateTime fechaPublicacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCarga estado = EstadoCarga.PUBLICADA;

    // Carrocería que pide la carga (null = cualquiera)
    @Enumerated(EnumType.STRING)
    @Column(name = "carroceria_requerida", length = 30)
    private TipoCarroceria carroceriaRequerida;

    @PrePersist
    protected void onCreate() {
        this.fechaPublicacion = LocalDateTime.now();
    }

    public Carga() {}

    // Getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public String getDireccionOrigen() { return direccionOrigen; }
    public void setDireccionOrigen(String direccionOrigen) { this.direccionOrigen = direccionOrigen; }

    public String getDireccionDestino() { return direccionDestino; }
    public void setDireccionDestino(String direccionDestino) { this.direccionDestino = direccionDestino; }

    public String getTipoMercancia() { return tipoMercancia; }
    public void setTipoMercancia(String tipoMercancia) { this.tipoMercancia = tipoMercancia; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }

    public BigDecimal getPrecioOfrecido() { return precioOfrecido; }
    public void setPrecioOfrecido(BigDecimal precioOfrecido) { this.precioOfrecido = precioOfrecido; }

    public LocalDateTime getFechaRecoleccion() { return fechaRecoleccion; }
    public void setFechaRecoleccion(LocalDateTime fechaRecoleccion) { this.fechaRecoleccion = fechaRecoleccion; }

    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public EstadoCarga getEstado() { return estado; }
    public void setEstado(EstadoCarga estado) { this.estado = estado; }

    public TipoCarroceria getCarroceriaRequerida() { return carroceriaRequerida; }
    public void setCarroceriaRequerida(TipoCarroceria carroceriaRequerida) { this.carroceriaRequerida = carroceriaRequerida; }

    /** true si ya pasó la fecha de recolección */
    public boolean isRecoleccionPasada() {
        return fechaRecoleccion != null && fechaRecoleccion.isBefore(LocalDateTime.now());
    }

    /** Si el vehículo tiene la carrocería que pide la carga. */
    public boolean admiteCarroceria(Vehiculo vehiculo) {
        return carroceriaRequerida == null || carroceriaRequerida == vehiculo.getTipoCarroceria();
    }

    /** Texto para listas y detalles: "Refrigerado" o "Cualquiera". */
    public String getCarroceriaTexto() {
        return carroceriaRequerida == null ? "Cualquiera" : carroceriaRequerida.getEtiqueta();
    }
}