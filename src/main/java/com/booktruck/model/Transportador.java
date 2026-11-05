package com.booktruck.model;

import com.booktruck.model.enums.CategoriaLicencia;
import com.booktruck.model.enums.EstadoVerificacion;
import jakarta.persistence.*;

@Entity
@Table(name = "transportadores")
public class Transportador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 20)
    private String cedula;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, length = 30)
    private String licencia;

    @Column(nullable = false)
    private Boolean disponible = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_verificacion", nullable = false, length = 20)
    private EstadoVerificacion estadoVerificacion = EstadoVerificacion.PENDIENTE;

    @Column(name = "documento_ruta", length = 255)
    private String documentoRuta;

    @Column(name = "documento_nombre_original", length = 255)
    private String documentoNombreOriginal;

    @Column(name = "motivo_rechazo", length = 255)
    private String motivoRechazo;

    public Transportador() {}

    // Getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getLicencia() { return licencia; }
    public void setLicencia(String licencia) { this.licencia = licencia; }

    public String getIniciales() { return com.booktruck.util.Textos.iniciales(nombreCompleto); }

    /** La licencia como categoría; null si el valor guardado no es C1, C2 ni C3. */
    public CategoriaLicencia getCategoriaLicencia() { return CategoriaLicencia.de(licencia); }

    /** true si su licencia le permite conducir el vehículo */
    public boolean puedeConducir(Vehiculo vehiculo) {
        CategoriaLicencia requerida = vehiculo.getLicenciaRequerida();
        CategoriaLicencia propia = getCategoriaLicencia();
        return requerida != null && propia != null && propia.permite(requerida);
    }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    public EstadoVerificacion getEstadoVerificacion() { return estadoVerificacion; }
    public void setEstadoVerificacion(EstadoVerificacion estadoVerificacion) { this.estadoVerificacion = estadoVerificacion; }
    public String getDocumentoRuta() { return documentoRuta; }
    public void setDocumentoRuta(String documentoRuta) { this.documentoRuta = documentoRuta; }

    public String getDocumentoNombreOriginal() { return documentoNombreOriginal; }
    public void setDocumentoNombreOriginal(String documentoNombreOriginal) { this.documentoNombreOriginal = documentoNombreOriginal; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
}