package com.booktruck.service;

import com.booktruck.dto.ResumenCalificacion;
import com.booktruck.exception.NegocioException;
import com.booktruck.model.Calificacion;
import com.booktruck.model.Empresa;
import com.booktruck.model.Transportador;
import com.booktruck.model.Viaje;
import com.booktruck.model.enums.EstadoViaje;
import com.booktruck.model.enums.TipoEvaluador;
import com.booktruck.repository.CalificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;

    public CalificacionService(CalificacionRepository calificacionRepository) {
        this.calificacionRepository = calificacionRepository;
    }

    // Filtros del admin. puntuacionMaxima permite "2 o menos" (casos con problema); null = sin filtro
    public List<Calificacion> filtrar(List<Calificacion> calificaciones, TipoEvaluador evaluador,
                                       Integer puntuacion, Integer puntuacionMaxima) {
        return calificaciones.stream()
                .filter(c -> evaluador == null || c.getEvaluadorTipo() == evaluador)
                .filter(c -> puntuacion == null || c.getPuntuacion().equals(puntuacion))
                .filter(c -> puntuacionMaxima == null || c.getPuntuacion() <= puntuacionMaxima)
                .toList();
    }

    public List<Calificacion> listarTodas(TipoEvaluador evaluadorFiltro) {
        if (evaluadorFiltro == null) {
            return calificacionRepository.findAllByOrderByFechaDesc();
        }
        return calificacionRepository.findByEvaluadorTipoOrderByFechaDesc(evaluadorFiltro);
    }

    public List<Calificacion> listarConProblema() {
        return calificacionRepository.findByPuntuacionLessThanEqualOrderByFechaDesc(2);
    }

    public List<Calificacion> listarRecibidasPorTransportador(Transportador transportador) {
        return calificacionRepository.findByTransportadorIdAndEvaluadorTipoOrderByFechaDesc(
                transportador.getId(), TipoEvaluador.EMPRESA);
    }

    public List<Calificacion> listarRecibidasPorEmpresa(Empresa empresa) {
        return calificacionRepository.findByEmpresaIdAndEvaluadorTipoOrderByFechaDesc(
                empresa.getId(), TipoEvaluador.TRANSPORTADOR);
    }

    public ResumenCalificacion resumenTransportador(Long transportadorId) {
        ResumenCalificacion resumen = calificacionRepository.resumenTransportador(transportadorId);
        return resumen != null ? resumen : new ResumenCalificacion(null, 0L);
    }

    public ResumenCalificacion resumenEmpresa(Long empresaId) {
        ResumenCalificacion resumen = calificacionRepository.resumenEmpresa(empresaId);
        return resumen != null ? resumen : new ResumenCalificacion(null, 0L);
    }

    /** Las calificaciones de un viaje (de la empresa y del transportador), para la página del viaje. */
    public List<Calificacion> deViaje(Viaje viaje) {
        return calificacionRepository.findByViajeId(viaje.getId());
    }

    public boolean yaCalificadoPorEmpresa(Viaje viaje) {
        return yaCalificado(viaje, TipoEvaluador.EMPRESA);
    }

    @Transactional
    public Calificacion calificarComoEmpresa(Viaje viaje, Integer puntuacion, String comentario) {
        return calificar(viaje, TipoEvaluador.EMPRESA, puntuacion, comentario);
    }

    public boolean yaCalificadoPorTransportador(Viaje viaje) {
        return yaCalificado(viaje, TipoEvaluador.TRANSPORTADOR);
    }

    @Transactional
    public Calificacion calificarComoTransportador(Viaje viaje, Integer puntuacion, String comentario) {
        return calificar(viaje, TipoEvaluador.TRANSPORTADOR, puntuacion, comentario);
    }

    private boolean yaCalificado(Viaje viaje, TipoEvaluador evaluadorTipo) {
        return calificacionRepository.findByViajeIdAndEvaluadorTipo(viaje.getId(), evaluadorTipo).isPresent();
    }

    private Calificacion calificar(Viaje viaje, TipoEvaluador evaluadorTipo, Integer puntuacion, String comentario) {
        if (viaje.getEstado() != EstadoViaje.FINALIZADO) {
            throw new NegocioException("Solo puedes calificar viajes finalizados.");
        }
        if (yaCalificado(viaje, evaluadorTipo)) {
            throw new NegocioException("Ya calificaste este viaje.");
        }
        if (puntuacion == null || puntuacion < 1 || puntuacion > 5) {
            throw new NegocioException("La puntuación debe estar entre 1 y 5.");
        }
        if (comentario != null && comentario.length() > 500) {
            throw new NegocioException("El comentario no puede tener más de 500 caracteres.");
        }

        Calificacion calificacion = new Calificacion();
        calificacion.setViaje(viaje);
        calificacion.setEmpresa(viaje.getCarga().getEmpresa());
        calificacion.setTransportador(viaje.getTransportador());
        calificacion.setEvaluadorTipo(evaluadorTipo);
        calificacion.setPuntuacion(puntuacion);
        calificacion.setComentario(comentario);
        return calificacionRepository.save(calificacion);
    }
}
