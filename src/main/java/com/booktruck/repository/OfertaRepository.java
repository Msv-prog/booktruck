package com.booktruck.repository;

import com.booktruck.model.Oferta;
import com.booktruck.model.enums.EstadoOferta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    List<Oferta> findByCargaId(Long cargaId);

    List<Oferta> findByTransportadorId(Long transportadorId);

    Optional<Oferta> findByCargaIdAndTransportadorId(Long cargaId, Long transportadorId);

    List<Oferta> findByCargaIdAndEstado(Long cargaId, EstadoOferta estado);

    long countByCarga_EmpresaIdAndEstado(Long empresaId, EstadoOferta estado);

    List<Oferta> findByCarga_EmpresaIdAndEstadoOrderByFechaOfertaDesc(Long empresaId, EstadoOferta estado);

    List<Oferta> findByCargaIdOrderByFechaOfertaDesc(Long cargaId);

    long countByTransportadorIdAndEstado(Long transportadorId, EstadoOferta estado);

    List<Oferta> findAllByOrderByFechaOfertaDesc();

    List<Oferta> findByEstadoOrderByFechaOfertaDesc(EstadoOferta estado);

    @Query("SELECT o FROM Oferta o WHERE (:desde IS NULL OR o.fechaOferta >= :desde) "
            + "AND (:hasta IS NULL OR o.fechaOferta <= :hasta)")
    List<Oferta> buscarPorRangoFecha(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}