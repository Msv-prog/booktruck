package com.booktruck.repository;

import com.booktruck.model.Viaje;
import com.booktruck.model.enums.EstadoViaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ViajeRepository extends JpaRepository<Viaje, Long> {

    List<Viaje> findByTransportadorId(Long transportadorId);

    List<Viaje> findByCargaId(Long cargaId);

    Optional<Viaje> findByOfertaId(Long ofertaId);

    long countByCarga_EmpresaIdAndEstado(Long empresaId, EstadoViaje estado);

    List<Viaje> findByCarga_EmpresaIdOrderByIdDesc(Long empresaId);

    long countByTransportadorIdAndEstado(Long transportadorId, EstadoViaje estado);

    List<Viaje> findByTransportadorIdOrderByIdDesc(Long transportadorId);

    long countByEstado(EstadoViaje estado);

    long countByTransportadorIdAndEstadoAndCanceladoPor(Long transportadorId, EstadoViaje estado,
                                                        com.booktruck.model.enums.RolUsuario canceladoPor);

    boolean existsByVehiculoIdAndEstadoIn(Long vehiculoId, List<EstadoViaje> estados);

    List<Viaje> findAllByOrderByIdDesc();

    List<Viaje> findByEstadoOrderByIdDesc(EstadoViaje estado);

    @Query("SELECT v FROM Viaje v WHERE v.fechaInicio IS NOT NULL "
            + "AND (:desde IS NULL OR v.fechaInicio >= :desde) AND (:hasta IS NULL OR v.fechaInicio <= :hasta)")
    List<Viaje> buscarPorRangoInicio(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT v FROM Viaje v WHERE v.estado = com.booktruck.model.enums.EstadoViaje.FINALIZADO "
            + "AND v.comisionMonto IS NOT NULL "
            + "AND (:desde IS NULL OR v.fechaFin >= :desde) AND (:hasta IS NULL OR v.fechaFin <= :hasta)")
    List<Viaje> buscarFinalizadosConComision(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}