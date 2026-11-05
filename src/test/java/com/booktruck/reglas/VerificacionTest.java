package com.booktruck.reglas;

import com.booktruck.model.Documento;
import com.booktruck.model.enums.DuenoDocumento;
import com.booktruck.model.enums.EstadoVerificacion;
import com.booktruck.model.enums.TipoDocumento;
import com.booktruck.service.VerificacionService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Estado de una cuenta calculado desde sus documentos:
 * RECHAZADO si hay alguno rechazado; APROBADO si todos los obligatorios están aprobados y vigentes; si no, PENDIENTE.
 */
class VerificacionTest {

    // estadoCalculado solo usa la lista que recibe, no los repositorios
    private final VerificacionService servicio = new VerificacionService(null, null, null, null, null);

    @Test
    void conTodosAprobadosYVigentesLaCuentaQuedaVerificada() {
        List<Documento> docs = List.of(
                doc(TipoDocumento.CEDULA, EstadoVerificacion.APROBADO, null),
                doc(TipoDocumento.LICENCIA_CONDUCCION, EstadoVerificacion.APROBADO, LocalDate.now().plusYears(2)));
        assertThat(servicio.estadoCalculado(DuenoDocumento.TRANSPORTADOR, docs)).isEqualTo(EstadoVerificacion.APROBADO);
    }

    @Test
    void unDocumentoVencidoBloqueaLaCuenta() {
        List<Documento> docs = List.of(
                doc(TipoDocumento.CEDULA, EstadoVerificacion.APROBADO, null),
                doc(TipoDocumento.LICENCIA_CONDUCCION, EstadoVerificacion.APROBADO, LocalDate.now().minusDays(1)));
        assertThat(servicio.estadoCalculado(DuenoDocumento.TRANSPORTADOR, docs)).isEqualTo(EstadoVerificacion.PENDIENTE);
    }

    @Test
    void unRechazoPesaMasQueTodoLoDemas() {
        List<Documento> docs = List.of(
                doc(TipoDocumento.CEDULA, EstadoVerificacion.APROBADO, null),
                doc(TipoDocumento.LICENCIA_CONDUCCION, EstadoVerificacion.RECHAZADO, LocalDate.now().plusYears(1)));
        assertThat(servicio.estadoCalculado(DuenoDocumento.TRANSPORTADOR, docs)).isEqualTo(EstadoVerificacion.RECHAZADO);
    }

    @Test
    void siFaltaUnDocumentoQuedaPendiente() {
        List<Documento> docs = List.of(doc(TipoDocumento.RUT, EstadoVerificacion.APROBADO, null));
        assertThat(servicio.estadoCalculado(DuenoDocumento.EMPRESA, docs)).isEqualTo(EstadoVerificacion.PENDIENTE);
    }

    @Test
    void elAvisoDeVencimientoEmpiezaTreintaDiasAntes() {
        Documento pronto = doc(TipoDocumento.SOAT, EstadoVerificacion.APROBADO, LocalDate.now().plusDays(Documento.DIAS_AVISO_VENCIMIENTO));
        Documento lejos = doc(TipoDocumento.SOAT, EstadoVerificacion.APROBADO, LocalDate.now().plusDays(Documento.DIAS_AVISO_VENCIMIENTO + 1));
        assertThat(pronto.isPorVencer()).isTrue();
        assertThat(lejos.isPorVencer()).isFalse();
    }

    private static Documento doc(TipoDocumento tipo, EstadoVerificacion estado, LocalDate vence) {
        Documento documento = new Documento();
        documento.setTipo(tipo);
        documento.setEstado(estado);
        documento.setFechaVencimiento(vence);
        return documento;
    }
}
