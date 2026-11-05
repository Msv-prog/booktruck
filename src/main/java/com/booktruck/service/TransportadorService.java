package com.booktruck.service;

import com.booktruck.dto.RegistroTransportadorForm;
import com.booktruck.dto.ResumenTransportadorDTO;
import com.booktruck.dto.TransportadorPerfilForm;
import com.booktruck.exception.NegocioException;
import com.booktruck.model.Transportador;
import com.booktruck.model.Usuario;
import com.booktruck.model.enums.CategoriaLicencia;
import com.booktruck.model.enums.EstadoOferta;
import com.booktruck.model.enums.EstadoVerificacion;
import com.booktruck.model.enums.EstadoViaje;
import com.booktruck.model.enums.RolUsuario;
import com.booktruck.util.Textos;
import com.booktruck.repository.OfertaRepository;
import com.booktruck.repository.TransportadorRepository;
import com.booktruck.repository.VehiculoRepository;
import com.booktruck.repository.ViajeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransportadorService {

    private final TransportadorRepository transportadorRepository;
    private final UsuarioService usuarioService;
    private final VehiculoRepository vehiculoRepository;
    private final OfertaRepository ofertaRepository;
    private final ViajeRepository viajeRepository;
    private final VerificacionService verificacionService;

    public TransportadorService(TransportadorRepository transportadorRepository, UsuarioService usuarioService,
                                 VehiculoRepository vehiculoRepository, OfertaRepository ofertaRepository,
                                 ViajeRepository viajeRepository, VerificacionService verificacionService) {
        this.transportadorRepository = transportadorRepository;
        this.usuarioService = usuarioService;
        this.vehiculoRepository = vehiculoRepository;
        this.ofertaRepository = ofertaRepository;
        this.viajeRepository = viajeRepository;
        this.verificacionService = verificacionService;
    }

    @Transactional
    public Transportador registrar(RegistroTransportadorForm form) {
        if (form.getCedula() == null || form.getCedula().isBlank()) {
            throw new NegocioException("La cédula es obligatoria.");
        }
        if (!form.getCedula().matches("^[0-9]{6,10}$")) {
            throw new NegocioException("La cédula debe tener entre 6 y 10 dígitos numéricos.");
        }
        validarDatosBasicos(form.getNombreCompleto(), form.getTelefono(), form.getLicencia());
        if (transportadorRepository.existsByCedula(form.getCedula())) {
            throw new NegocioException("Ya existe un transportador registrado con esa cédula.");
        }

        Usuario usuario = usuarioService.crearUsuarioBase(
                form.getEmail(), form.getPassword(), form.getConfirmarPassword(), RolUsuario.TRANSPORTADOR);

        Transportador transportador = new Transportador();
        transportador.setUsuario(usuario);
        transportador.setCedula(form.getCedula());
        transportador.setNombreCompleto(form.getNombreCompleto());
        transportador.setTelefono(form.getTelefono());
        transportador.setLicencia(form.getLicencia());
        transportador.setDisponible(false);

        return transportadorRepository.save(transportador);
    }

    public List<Transportador> listarTodos() {
        return transportadorRepository.findAllByOrderByIdDesc();
    }

    // Filtros del admin (null o vacío = sin filtro)
    public List<Transportador> filtrar(List<Transportador> transportadores, EstadoVerificacion verificacion,
                                        Boolean disponible, String licencia, String busqueda) {
        return transportadores.stream()
                .filter(t -> verificacion == null || verificacionService.estadoTransportador(t) == verificacion)
                .filter(t -> disponible == null || t.getDisponible().equals(disponible))
                .filter(t -> Textos.vacio(licencia) || licencia.equals(t.getLicencia()))
                .filter(t -> Textos.coincide(busqueda, t.getNombreCompleto(), t.getCedula()))
                .toList();
    }

    public Transportador buscarPorUsuarioId(Long usuarioId) {
        return transportadorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new NegocioException("No se encontró el transportador asociado al usuario."));
    }

    @Transactional
    public Transportador actualizarPerfil(Transportador transportador, TransportadorPerfilForm form) {
        validarDatosBasicos(form.getNombreCompleto(), form.getTelefono(), form.getLicencia());

        boolean cambiaLicencia = !form.getLicencia().equals(transportador.getLicencia());
        if (cambiaLicencia) {
            // No se puede bajar de categoría si quedan vehículos que ya no podría conducir
            CategoriaLicencia nueva = CategoriaLicencia.de(form.getLicencia());
            List<String> incompatibles = vehiculoRepository.findByTransportadorId(transportador.getId()).stream()
                    .filter(v -> v.getLicenciaRequerida() != null && !nueva.permite(v.getLicenciaRequerida()))
                    .map(v -> v.getPlaca() + " (requiere " + v.getLicenciaRequerida().name() + ")")
                    .toList();
            if (!incompatibles.isEmpty()) {
                throw new NegocioException("Con licencia " + nueva.name() + " no podrías conducir: "
                        + String.join(", ", incompatibles) + ". Desactivar el vehículo no basta: debe ser de la categoría de tu licencia.");
            }
        }

        transportador.setNombreCompleto(form.getNombreCompleto());
        transportador.setTelefono(form.getTelefono());
        transportador.setLicencia(form.getLicencia());
        Transportador guardado = transportadorRepository.save(transportador);
        if (cambiaLicencia) {
            verificacionService.pedirNuevaLicencia(guardado, form.getLicencia());
        }
        return guardado;
    }

    /** Si al guardar este formulario se cambiaría la categoría (para avisar que debe subir la licencia otra vez). */
    public boolean cambiaLicencia(Transportador transportador, TransportadorPerfilForm form) {
        return form.getLicencia() != null && !form.getLicencia().equals(transportador.getLicencia());
    }

    @Transactional
    public Transportador alternarDisponibilidad(Transportador transportador) {
        transportador.setDisponible(!transportador.getDisponible());
        return transportadorRepository.save(transportador);
    }

    public Transportador buscarPorId(Long transportadorId) {
        return transportadorRepository.findById(transportadorId)
                .orElseThrow(() -> new NegocioException("El transportador no existe."));
    }

    public ResumenTransportadorDTO obtenerResumen(Long transportadorId) {
        long vehiculosActivos = vehiculoRepository.countByTransportadorIdAndActivo(transportadorId, true);
        long ofertasPendientes = ofertaRepository.countByTransportadorIdAndEstado(transportadorId, EstadoOferta.PENDIENTE);
        long viajesEnCurso = viajeRepository.countByTransportadorIdAndEstado(transportadorId, EstadoViaje.EN_CURSO);
        return new ResumenTransportadorDTO(vehiculosActivos, ofertasPendientes, viajesEnCurso);
    }

    private void validarDatosBasicos(String nombreCompleto, String telefono, String licencia) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new NegocioException("El nombre completo es obligatorio.");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new NegocioException("El teléfono es obligatorio.");
        }
        if (!telefono.matches("^[0-9]{7,10}$")) {
            throw new NegocioException("El teléfono debe tener entre 7 y 10 dígitos numéricos.");
        }
        if (licencia == null || licencia.isBlank()) {
            throw new NegocioException("La licencia es obligatoria.");
        }
        if (CategoriaLicencia.de(licencia) == null) {
            throw new NegocioException("La categoría de licencia debe ser C1, C2 o C3.");
        }
    }
}
