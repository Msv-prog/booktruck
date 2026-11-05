package com.booktruck.repository;

import com.booktruck.model.SeguimientoUbicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeguimientoUbicacionRepository extends JpaRepository<SeguimientoUbicacion, Long> {

    List<SeguimientoUbicacion> findByViajeIdOrderByFechaHoraDesc(Long viajeId);
}