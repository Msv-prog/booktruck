package com.booktruck.repository;

import com.booktruck.model.Documento;
import com.booktruck.model.enums.EstadoVerificacion;
import com.booktruck.model.enums.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByEmpresaId(Long empresaId);

    List<Documento> findByTransportadorId(Long transportadorId);

    List<Documento> findByVehiculoId(Long vehiculoId);

    Optional<Documento> findByEmpresaIdAndTipo(Long empresaId, TipoDocumento tipo);

    Optional<Documento> findByTransportadorIdAndTipo(Long transportadorId, TipoDocumento tipo);

    Optional<Documento> findByVehiculoIdAndTipo(Long vehiculoId, TipoDocumento tipo);

    List<Documento> findByEstado(EstadoVerificacion estado);

    long countByEstado(EstadoVerificacion estado);

    List<Documento> findByEstadoAndFechaVencimientoLessThanEqualOrderByFechaVencimiento(EstadoVerificacion estado, LocalDate hasta);

    // Cuántos dueños distintos tienen al menos un documento en ese estado (contadores del admin)
    @Query("SELECT COUNT(DISTINCT d.empresa.id) FROM Documento d WHERE d.estado = :estado AND d.empresa IS NOT NULL")
    long contarEmpresasConDocumentoEn(@Param("estado") EstadoVerificacion estado);

    @Query("SELECT COUNT(DISTINCT d.transportador.id) FROM Documento d WHERE d.estado = :estado AND d.transportador IS NOT NULL")
    long contarTransportadoresConDocumentoEn(@Param("estado") EstadoVerificacion estado);

    @Query("SELECT COUNT(DISTINCT d.vehiculo.id) FROM Documento d WHERE d.estado = :estado AND d.vehiculo IS NOT NULL")
    long contarVehiculosConDocumentoEn(@Param("estado") EstadoVerificacion estado);
}
