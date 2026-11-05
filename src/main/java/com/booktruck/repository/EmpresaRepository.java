package com.booktruck.repository;

import com.booktruck.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByUsuarioId(Long usuarioId);

    boolean existsByNit(String nit);

    List<Empresa> findAllByOrderByIdDesc();
}