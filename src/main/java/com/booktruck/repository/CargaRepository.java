package com.booktruck.repository;

import com.booktruck.model.Carga;
import com.booktruck.model.enums.EstadoCarga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CargaRepository extends JpaRepository<Carga, Long> {

    List<Carga> findByEmpresaId(Long empresaId);

    List<Carga> findByEstado(EstadoCarga estado);

    List<Carga> findByEmpresaIdAndEstado(Long empresaId, EstadoCarga estado);

    long countByEmpresaIdAndEstadoIn(Long empresaId, List<EstadoCarga> estados);

    List<Carga> findTop5ByEmpresaIdOrderByFechaPublicacionDesc(Long empresaId);

    List<Carga> findByEmpresaIdOrderByFechaPublicacionDesc(Long empresaId);

    List<Carga> findByEmpresaIdAndEstadoOrderByFechaPublicacionDesc(Long empresaId, EstadoCarga estado);

    @Query("SELECT c FROM Carga c WHERE c.estado = :estado "
            + "AND (:origen IS NULL OR LOWER(c.origen) LIKE LOWER(CONCAT('%', :origen, '%'))) "
            + "AND (:destino IS NULL OR LOWER(c.destino) LIKE LOWER(CONCAT('%', :destino, '%'))) "
            + "AND (:tipoMercancia IS NULL OR LOWER(c.tipoMercancia) LIKE LOWER(CONCAT('%', :tipoMercancia, '%'))) "
            + "AND (:pesoMaximo IS NULL OR c.peso <= :pesoMaximo) "
            + "AND (:precioMinimo IS NULL OR c.precioOfrecido >= :precioMinimo) "
            + "AND (:precioMaximo IS NULL OR c.precioOfrecido <= :precioMaximo) "
            + "ORDER BY c.fechaPublicacion DESC")
    List<Carga> buscarDisponiblesConFiltros(@Param("estado") EstadoCarga estado,
                                             @Param("origen") String origen,
                                             @Param("destino") String destino,
                                             @Param("tipoMercancia") String tipoMercancia,
                                             @Param("pesoMaximo") BigDecimal pesoMaximo,
                                             @Param("precioMinimo") BigDecimal precioMinimo,
                                             @Param("precioMaximo") BigDecimal precioMaximo);

    long countByEstadoIn(List<EstadoCarga> estados);

    List<Carga> findByEstadoAndFechaRecoleccionBefore(EstadoCarga estado, LocalDateTime fecha);

    long countByEmpresaId(Long empresaId);

    List<Carga> findAllByOrderByFechaPublicacionDesc();

    List<Carga> findByEstadoOrderByFechaPublicacionDesc(EstadoCarga estado);

    @Query("SELECT c FROM Carga c WHERE (:desde IS NULL OR c.fechaPublicacion >= :desde) "
            + "AND (:hasta IS NULL OR c.fechaPublicacion <= :hasta)")
    List<Carga> buscarPorRangoPublicacion(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}