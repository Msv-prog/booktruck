package com.booktruck.service;

import com.booktruck.dto.ReporteAdminDTO;
import com.booktruck.model.Calificacion;
import com.booktruck.model.Carga;
import com.booktruck.model.Oferta;
import com.booktruck.model.Viaje;
import com.booktruck.model.enums.EstadoCarga;
import com.booktruck.model.enums.EstadoViaje;
import com.booktruck.model.enums.TipoEvaluador;
import com.booktruck.repository.CalificacionRepository;
import com.booktruck.repository.CargaRepository;
import com.booktruck.repository.OfertaRepository;
import com.booktruck.repository.ViajeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
public class AdminReporteService {

    private final CargaRepository cargaRepository;
    private final OfertaRepository ofertaRepository;
    private final ViajeRepository viajeRepository;
    private final CalificacionRepository calificacionRepository;

    public AdminReporteService(CargaRepository cargaRepository, OfertaRepository ofertaRepository,
                                ViajeRepository viajeRepository, CalificacionRepository calificacionRepository) {
        this.cargaRepository = cargaRepository;
        this.ofertaRepository = ofertaRepository;
        this.viajeRepository = viajeRepository;
        this.calificacionRepository = calificacionRepository;
    }

    public ReporteAdminDTO generar(LocalDate desde, LocalDate hasta) {
        LocalDateTime desdeFecha = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaFecha = hasta != null ? hasta.atTime(23, 59, 59) : null;

        List<Carga> cargas = cargaRepository.buscarPorRangoPublicacion(desdeFecha, hastaFecha);
        Map<EstadoCarga, Long> cargasPorEstado = cargas.stream()
                .collect(Collectors.groupingBy(Carga::getEstado, Collectors.counting()));
        BigDecimal sumaPeso = cargas.stream().map(Carga::getPeso).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumaPrecioOfrecido = cargas.stream().map(Carga::getPrecioOfrecido).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Oferta> ofertas = ofertaRepository.buscarPorRangoFecha(desdeFecha, hastaFecha);
        BigDecimal montoPromedioOfertas = ofertas.isEmpty() ? BigDecimal.ZERO
                : ofertas.stream().map(Oferta::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(ofertas.size()), 2, RoundingMode.HALF_UP);

        List<Viaje> viajes = viajeRepository.buscarPorRangoInicio(desdeFecha, hastaFecha);
        Map<EstadoViaje, Long> viajesPorEstado = viajes.stream()
                .collect(Collectors.groupingBy(Viaje::getEstado, Collectors.counting()));

        List<Viaje> viajesConComision = viajeRepository.buscarFinalizadosConComision(desdeFecha, hastaFecha);
        BigDecimal totalComisiones = viajesConComision.stream()
                .map(Viaje::getComisionMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorTransportado = viajesConComision.stream()
                .map(v -> v.getOferta().getMonto()).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Calificacion> calificaciones = calificacionRepository.buscarPorRangoFecha(desdeFecha, hastaFecha);
        Double promedioEmpresa = promedioPuntuacion(calificaciones, TipoEvaluador.EMPRESA);
        Double promedioTransportador = promedioPuntuacion(calificaciones, TipoEvaluador.TRANSPORTADOR);

        return new ReporteAdminDTO(
                cargas.size(), cargasPorEstado, sumaPeso, sumaPrecioOfrecido,
                ofertas.size(), montoPromedioOfertas,
                viajes.size(), viajesPorEstado,
                viajesConComision.size(), valorTransportado, totalComisiones,
                calificaciones.size(), promedioEmpresa, promedioTransportador);
    }

    private Double promedioPuntuacion(List<Calificacion> calificaciones, TipoEvaluador evaluadorTipo) {
        OptionalDouble promedio = calificaciones.stream()
                .filter(c -> c.getEvaluadorTipo() == evaluadorTipo)
                .mapToInt(Calificacion::getPuntuacion)
                .average();
        return promedio.isPresent() ? promedio.getAsDouble() : null;
    }
}
