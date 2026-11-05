package com.booktruck.repository;

import com.booktruck.dto.ResumenCalificacion;
import com.booktruck.model.Calificacion;
import com.booktruck.model.enums.TipoEvaluador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    List<Calificacion> findByViajeId(Long viajeId);

    Optional<Calificacion> findByViajeIdAndEvaluadorTipo(Long viajeId, TipoEvaluador evaluadorTipo);

    List<Calificacion> findAllByOrderByFechaDesc();

    List<Calificacion> findByEvaluadorTipoOrderByFechaDesc(TipoEvaluador evaluadorTipo);

    List<Calificacion> findByPuntuacionLessThanEqualOrderByFechaDesc(Integer puntuacionMaxima);

    List<Calificacion> findByTransportadorIdAndEvaluadorTipoOrderByFechaDesc(Long transportadorId, TipoEvaluador evaluadorTipo);

    List<Calificacion> findByEmpresaIdAndEvaluadorTipoOrderByFechaDesc(Long empresaId, TipoEvaluador evaluadorTipo);

    @Query("SELECT new com.booktruck.dto.ResumenCalificacion(AVG(c.puntuacion), COUNT(c)) FROM Calificacion c "
            + "WHERE c.transportador.id = :transportadorId AND c.evaluadorTipo = com.booktruck.model.enums.TipoEvaluador.EMPRESA")
    ResumenCalificacion resumenTransportador(@Param("transportadorId") Long transportadorId);

    @Query("SELECT new com.booktruck.dto.ResumenCalificacion(AVG(c.puntuacion), COUNT(c)) FROM Calificacion c "
            + "WHERE c.empresa.id = :empresaId AND c.evaluadorTipo = com.booktruck.model.enums.TipoEvaluador.TRANSPORTADOR")
    ResumenCalificacion resumenEmpresa(@Param("empresaId") Long empresaId);

    @Query("SELECT c FROM Calificacion c WHERE (:desde IS NULL OR c.fecha >= :desde) "
            + "AND (:hasta IS NULL OR c.fecha <= :hasta)")
    List<Calificacion> buscarPorRangoFecha(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}