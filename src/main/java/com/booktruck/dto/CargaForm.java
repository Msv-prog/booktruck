package com.booktruck.dto;

import com.booktruck.model.enums.TipoCarroceria;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CargaForm {

    private String origen;
    private String destino;
    private String direccionOrigen;
    private String direccionDestino;
    private String tipoMercancia;
    private BigDecimal peso;
    private BigDecimal precioOfrecido;
    private TipoCarroceria carroceriaRequerida;

    /** Formulario con los datos actuales de la carga, para editarla. */
    public static CargaForm desde(com.booktruck.model.Carga carga) {
        CargaForm form = new CargaForm();
        form.setOrigen(carga.getOrigen());
        form.setDestino(carga.getDestino());
        form.setDireccionOrigen(carga.getDireccionOrigen());
        form.setDireccionDestino(carga.getDireccionDestino());
        form.setTipoMercancia(carga.getTipoMercancia());
        form.setPeso(carga.getPeso());
        form.setPrecioOfrecido(carga.getPrecioOfrecido());
        form.setCarroceriaRequerida(carga.getCarroceriaRequerida());
        // Una carga vencida necesita fecha nueva: se deja vacía para que la empresa la escoja
        form.setFechaRecoleccion(carga.isRecoleccionPasada() ? null : carga.getFechaRecoleccion());
        return form;
    }

    public TipoCarroceria getCarroceriaRequerida() { return carroceriaRequerida; }
    public void setCarroceriaRequerida(TipoCarroceria carroceriaRequerida) { this.carroceriaRequerida = carroceriaRequerida; }

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaRecoleccion;

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
}
