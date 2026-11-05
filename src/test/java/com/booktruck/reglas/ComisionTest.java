package com.booktruck.reglas;

import com.booktruck.model.Carga;
import com.booktruck.model.Empresa;
import com.booktruck.model.Oferta;
import com.booktruck.model.Transportador;
import com.booktruck.model.Viaje;
import com.booktruck.model.enums.EstadoViaje;
import com.booktruck.repository.OfertaRepository;
import com.booktruck.repository.ViajeRepository;
import com.booktruck.service.CargaService;
import com.booktruck.service.FileStorageService;
import com.booktruck.service.NotificacionService;
import com.booktruck.service.ViajeService;
import com.booktruck.exception.NegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** La comisión simulada (3%) se descuenta al transportador cuando la empresa confirma la recepción. */
class ComisionTest {

    private final ViajeRepository viajeRepository = mock(ViajeRepository.class);
    private final ViajeService viajeService = new ViajeService(viajeRepository, mock(OfertaRepository.class),
            mock(CargaService.class), mock(NotificacionService.class), mock(FileStorageService.class), new BigDecimal("3"));

    @Test
    void alConfirmarSeCobraElTresPorCientoDelMontoAcordado() {
        Viaje viaje = viajeEntregado(new BigDecimal("1800000"));
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje));

        viajeService.confirmarRecepcion(viaje.getCarga().getEmpresa(), 1L);

        assertThat(viaje.getEstado()).isEqualTo(EstadoViaje.FINALIZADO);
        assertThat(viaje.getComisionMonto()).isEqualByComparingTo("54000");
        assertThat(viaje.getNetoTransportador()).isEqualByComparingTo("1746000");
    }

    @Test
    void noSePuedeConfirmarUnViajeQueNoFueEntregado() {
        Viaje viaje = viajeEntregado(new BigDecimal("1000000"));
        viaje.setEstado(EstadoViaje.EN_CURSO);
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje));

        assertThatThrownBy(() -> viajeService.confirmarRecepcion(viaje.getCarga().getEmpresa(), 1L))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void otraEmpresaNoPuedeConfirmarElViaje() {
        Viaje viaje = viajeEntregado(new BigDecimal("1000000"));
        when(viajeRepository.findById(1L)).thenReturn(Optional.of(viaje));
        Empresa otra = new Empresa();
        otra.setId(99L);

        assertThatThrownBy(() -> viajeService.confirmarRecepcion(otra, 1L)).isInstanceOf(NegocioException.class);
    }

    private static Viaje viajeEntregado(BigDecimal monto) {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setRazonSocial("Empresa de prueba");
        Carga carga = new Carga();
        carga.setEmpresa(empresa);
        carga.setOrigen("Medellín");
        carga.setDestino("Barranquilla");
        Oferta oferta = new Oferta();
        oferta.setMonto(monto);
        Viaje viaje = new Viaje();
        viaje.setId(1L);
        viaje.setCarga(carga);
        viaje.setOferta(oferta);
        viaje.setTransportador(new Transportador());
        viaje.setEstado(EstadoViaje.ENTREGADO);
        return viaje;
    }
}
