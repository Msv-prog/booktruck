package com.booktruck.service;

import com.booktruck.model.Carga;
import com.booktruck.model.Empresa;
import com.booktruck.model.Transportador;
import com.booktruck.model.Viaje;
import com.booktruck.model.enums.EstadoCarga;
import com.booktruck.model.enums.EstadoViaje;
import com.booktruck.repository.CargaRepository;
import com.booktruck.repository.ViajeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/** Datos para las gráficas de las páginas de inicio (Chart.js) */
@Service
public class EstadisticasService {

    private static final int MESES = 6;
    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MMM yyyy", Locale.forLanguageTag("es-CO"));

    private static final Map<EstadoCarga, String> NOMBRES_CARGA = new LinkedHashMap<>();
    static {
        NOMBRES_CARGA.put(EstadoCarga.PUBLICADA, "Publicadas");
        NOMBRES_CARGA.put(EstadoCarga.ASIGNADA, "Asignadas");
        NOMBRES_CARGA.put(EstadoCarga.COMPLETADA, "Completadas");
        NOMBRES_CARGA.put(EstadoCarga.VENCIDA, "Vencidas");
        NOMBRES_CARGA.put(EstadoCarga.CANCELADA, "Canceladas");
    }

    private final CargaRepository cargaRepository;
    private final ViajeRepository viajeRepository;

    public EstadisticasService(CargaRepository cargaRepository, ViajeRepository viajeRepository) {
        this.cargaRepository = cargaRepository;
        this.viajeRepository = viajeRepository;
    }

    /** Admin: viajes finalizados y comisiones de los últimos 6 meses, y cargas por estado. */
    public Map<String, Object> admin() {
        List<Viaje> finalizados = viajeRepository.findByEstadoOrderByIdDesc(EstadoViaje.FINALIZADO);
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("meses", etiquetasMeses());
        datos.put("viajesPorMes", porMes(finalizados, v -> BigDecimal.ONE));
        datos.put("comisionesPorMes", porMes(finalizados, v -> v.getComisionMonto() == null ? BigDecimal.ZERO : v.getComisionMonto()));
        datos.put("cargasPorEstado", cargasPorEstado(cargaRepository.findAll()));
        return datos;
    }

    /** Empresa: sus cargas por estado y lo que pagó en fletes por mes. */
    public Map<String, Object> empresa(Empresa empresa) {
        List<Viaje> finalizados = viajeRepository.findByCarga_EmpresaIdOrderByIdDesc(empresa.getId()).stream()
                .filter(v -> v.getEstado() == EstadoViaje.FINALIZADO).toList();
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("meses", etiquetasMeses());
        datos.put("gastoPorMes", porMes(finalizados, v -> v.getOferta().getMonto()));
        datos.put("cargasPorEstado", cargasPorEstado(cargaRepository.findByEmpresaIdOrderByFechaPublicacionDesc(empresa.getId())));
        return datos;
    }

    /** Transportador: lo que recibió (neto, sin la comisión) por mes y sus viajes por estado. */
    public Map<String, Object> transportador(Transportador transportador) {
        List<Viaje> viajes = viajeRepository.findByTransportadorIdOrderByIdDesc(transportador.getId());
        List<Viaje> finalizados = viajes.stream().filter(v -> v.getEstado() == EstadoViaje.FINALIZADO).toList();
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("meses", etiquetasMeses());
        datos.put("gananciasPorMes", porMes(finalizados, v -> v.getNetoTransportador() == null ? BigDecimal.ZERO : v.getNetoTransportador()));

        Map<String, Long> porEstado = new LinkedHashMap<>();
        porEstado.put("Programados", contar(viajes, EstadoViaje.PROGRAMADO));
        porEstado.put("En curso", contar(viajes, EstadoViaje.EN_CURSO));
        porEstado.put("Entregados", contar(viajes, EstadoViaje.ENTREGADO));
        porEstado.put("Finalizados", contar(viajes, EstadoViaje.FINALIZADO));
        porEstado.put("Cancelados", contar(viajes, EstadoViaje.CANCELADO));
        datos.put("viajesPorEstado", dona(porEstado));
        return datos;
    }

    private static long contar(List<Viaje> viajes, EstadoViaje estado) {
        return viajes.stream().filter(v -> v.getEstado() == estado).count();
    }

    private static Map<String, Object> cargasPorEstado(List<Carga> cargas) {
        Map<String, Long> conteo = new LinkedHashMap<>();
        NOMBRES_CARGA.forEach((estado, nombre) ->
                conteo.put(nombre, cargas.stream().filter(c -> c.getEstado() == estado).count()));
        return dona(conteo);
    }

    // Formato para una gráfica de dona: etiquetas y valores, sin las categorías en cero
    private static Map<String, Object> dona(Map<String, Long> conteo) {
        List<String> etiquetas = new ArrayList<>();
        List<Long> valores = new ArrayList<>();
        conteo.forEach((etiqueta, valor) -> {
            if (valor > 0) {
                etiquetas.add(etiqueta);
                valores.add(valor);
            }
        });
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("etiquetas", etiquetas);
        datos.put("valores", valores);
        return datos;
    }

    private static List<String> etiquetasMeses() {
        List<String> etiquetas = new ArrayList<>();
        YearMonth actual = YearMonth.now();
        for (int i = MESES - 1; i >= 0; i--) {
            etiquetas.add(actual.minusMonths(i).format(FORMATO_MES));
        }
        return etiquetas;
    }

    // Suma un valor por mes de finalización, para los últimos 6 meses (el más antiguo primero)
    private static List<BigDecimal> porMes(List<Viaje> viajes, Function<Viaje, BigDecimal> valor) {
        YearMonth actual = YearMonth.now();
        List<BigDecimal> totales = new ArrayList<>();
        for (int i = MESES - 1; i >= 0; i--) {
            YearMonth mes = actual.minusMonths(i);
            totales.add(viajes.stream()
                    .filter(v -> v.getFechaFin() != null && YearMonth.from(v.getFechaFin()).equals(mes))
                    .map(valor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return totales;
    }
}
