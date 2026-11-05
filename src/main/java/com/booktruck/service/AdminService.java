package com.booktruck.service;

import com.booktruck.dto.AdminResumenDTO;
import com.booktruck.model.enums.EstadoCarga;
import com.booktruck.model.enums.EstadoViaje;
import com.booktruck.repository.CalificacionRepository;
import com.booktruck.repository.CargaRepository;
import com.booktruck.repository.EmpresaRepository;
import com.booktruck.repository.OfertaRepository;
import com.booktruck.repository.TransportadorRepository;
import com.booktruck.repository.UsuarioRepository;
import com.booktruck.repository.VehiculoRepository;
import com.booktruck.repository.ViajeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final TransportadorRepository transportadorRepository;
    private final VehiculoRepository vehiculoRepository;
    private final CargaRepository cargaRepository;
    private final OfertaRepository ofertaRepository;
    private final ViajeRepository viajeRepository;
    private final CalificacionRepository calificacionRepository;

    public AdminService(UsuarioRepository usuarioRepository, EmpresaRepository empresaRepository,
                         TransportadorRepository transportadorRepository, VehiculoRepository vehiculoRepository,
                         CargaRepository cargaRepository, OfertaRepository ofertaRepository,
                         ViajeRepository viajeRepository, CalificacionRepository calificacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.transportadorRepository = transportadorRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.cargaRepository = cargaRepository;
        this.ofertaRepository = ofertaRepository;
        this.viajeRepository = viajeRepository;
        this.calificacionRepository = calificacionRepository;
    }

    public AdminResumenDTO obtenerResumenGeneral() {
        long cargasActivas = cargaRepository.countByEstadoIn(
                List.of(EstadoCarga.PUBLICADA, EstadoCarga.EN_PROCESO, EstadoCarga.ASIGNADA));

        return new AdminResumenDTO(
                usuarioRepository.count(),
                empresaRepository.count(),
                transportadorRepository.count(),
                vehiculoRepository.count(),
                cargaRepository.count(),
                cargasActivas,
                ofertaRepository.count(),
                viajeRepository.count(),
                viajeRepository.countByEstado(EstadoViaje.EN_CURSO),
                calificacionRepository.count());
    }
}
