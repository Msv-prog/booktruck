package com.booktruck.model;

import com.booktruck.model.enums.EstadoVerificacion;
import jakarta.persistence.*;

@Entity
@Table(name = "empresas")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 20)
    private String nit;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, length = 80)
    private String ciudad;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_verificacion", nullable = false, length = 20)
    private EstadoVerificacion estadoVerificacion = EstadoVerificacion.PENDIENTE;

    @Column(name = "documento_ruta", length = 255)
    private String documentoRuta;

    @Column(name = "documento_nombre_original", length = 255)
    private String documentoNombreOriginal;

    @Column(name = "motivo_rechazo", length = 255)
    private String motivoRechazo;

    public Empresa() {}

    // Getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getRazonSocial() { return razonSocial; }

    public String getIniciales() { return com.booktruck.util.Textos.iniciales(razonSocial); }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public EstadoVerificacion getEstadoVerificacion() { return estadoVerificacion; }
    public void setEstadoVerificacion(EstadoVerificacion estadoVerificacion) { this.estadoVerificacion = estadoVerificacion; }
    public String getDocumentoRuta() { return documentoRuta; }
    public void setDocumentoRuta(String documentoRuta) { this.documentoRuta = documentoRuta; }

    public String getDocumentoNombreOriginal() { return documentoNombreOriginal; }
    public void setDocumentoNombreOriginal(String documentoNombreOriginal) { this.documentoNombreOriginal = documentoNombreOriginal; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
}