package com.booktruck.repository;

import com.booktruck.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    List<Vehiculo> findByTransportadorId(Long transportadorId);

    boolean existsByPlaca(String placa);

    long countByTransportadorIdAndActivo(Long transportadorId, Boolean activo);

    long countByTransportadorId(Long transportadorId);

    List<Vehiculo> findAllByOrderByIdDesc();
}