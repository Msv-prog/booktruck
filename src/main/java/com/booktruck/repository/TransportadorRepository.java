package com.booktruck.repository;

import com.booktruck.model.Transportador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransportadorRepository extends JpaRepository<Transportador, Long> {

    Optional<Transportador> findByUsuarioId(Long usuarioId);

    boolean existsByCedula(String cedula);

    List<Transportador> findAllByOrderByIdDesc();
}