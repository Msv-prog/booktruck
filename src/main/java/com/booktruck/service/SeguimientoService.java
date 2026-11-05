package com.booktruck.service;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.SeguimientoUbicacion;
import com.booktruck.model.Viaje;
import com.booktruck.model.enums.EstadoViaje;
import com.booktruck.repository.SeguimientoUbicacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeguimientoService {

    private static final int MINUTOS_ENTRE_ACTUALIZACIONES = 30;

    private final SeguimientoUbicacionRepository seguimientoUbicacionRepository;

    public SeguimientoService(SeguimientoUbicacionRepository seguimientoUbicacionRepository) {
        this.seguimientoUbicacionRepository = seguimientoUbicacionRepository;
    }

    public List<SeguimientoUbicacion> listarPorViaje(Viaje viaje) {
        return seguimientoUbicacionRepository.findByViajeIdOrderByFechaHoraDesc(viaje.getId());
    }

    public boolean requiereActualizacion(List<SeguimientoUbicacion> puntos) {
        if (puntos.isEmpty()) {
            return true;
        }
        LocalDateTime ultima = puntos.get(0).getFechaHora();
        return ultima.isBefore(LocalDateTime.now().minusMinutes(MINUTOS_ENTRE_ACTUALIZACIONES));
    }

    @Transactional
    public SeguimientoUbicacion registrar(Viaje viaje, BigDecimal latitud, BigDecimal longitud, String observacion) {
        if (viaje.getEstado() != EstadoViaje.EN_CURSO) {
            throw new NegocioException("Solo puedes registrar ubicación en viajes que estén en curso.");
        }
        if (latitud == null || longitud == null) {
            throw new NegocioException("No se pudo obtener tu ubicación. Verifica los permisos del navegador.");
        }

        SeguimientoUbicacion punto = new SeguimientoUbicacion();
        punto.setViaje(viaje);
        punto.setLatitud(latitud);
        punto.setLongitud(longitud);
        punto.setObservacion(observacion);
        return seguimientoUbicacionRepository.save(punto);
    }
}
