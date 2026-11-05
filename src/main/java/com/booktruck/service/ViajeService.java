package com.booktruck.service;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.Empresa;
import com.booktruck.model.Oferta;
import com.booktruck.model.Transportador;
import com.booktruck.model.Viaje;
import com.booktruck.model.enums.EstadoOferta;
import com.booktruck.model.enums.EstadoViaje;
import com.booktruck.model.enums.MotivoCancelacion;
import com.booktruck.model.enums.RolUsuario;
import com.booktruck.repository.OfertaRepository;
import com.booktruck.repository.ViajeRepository;
import com.booktruck.util.Textos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ViajeService {

    private final ViajeRepository viajeRepository;
    private final OfertaRepository ofertaRepository;
    private final CargaService cargaService;
    private final NotificacionService notificacionService;
    private final FileStorageService fileStorageService;
    private final BigDecimal porcentajeComision;

    public ViajeService(ViajeRepository viajeRepository, OfertaRepository ofertaRepository, CargaService cargaService,
                        NotificacionService notificacionService, FileStorageService fileStorageService,
                        @Value("${booktruck.comision.porcentaje}") BigDecimal porcentajeComision) {
        this.viajeRepository = viajeRepository;
        this.ofertaRepository = ofertaRepository;
        this.cargaService = cargaService;
        this.notificacionService = notificacionService;
        this.fileStorageService = fileStorageService;
        this.porcentajeComision = porcentajeComision;
    }

    public BigDecimal getPorcentajeComision() {
        return porcentajeComision;
    }

    public boolean tieneViajeActivo(Long vehiculoId) {
        return viajeRepository.existsByVehiculoIdAndEstadoIn(vehiculoId,
                List.of(EstadoViaje.PROGRAMADO, EstadoViaje.EN_CURSO));
    }

    public boolean tieneViajeAsociado(Long ofertaId) {
        return viajeRepository.findByOfertaId(ofertaId).isPresent();
    }

    public long contarEntregadosPorEmpresa(Long empresaId) {
        return viajeRepository.countByCarga_EmpresaIdAndEstado(empresaId, EstadoViaje.ENTREGADO);
    }

    public long contarProgramadosPorTransportador(Long transportadorId) {
        return viajeRepository.countByTransportadorIdAndEstado(transportadorId, EstadoViaje.PROGRAMADO);
    }

    public List<Viaje> listarPorEmpresa(Empresa empresa) {
        return viajeRepository.findByCarga_EmpresaIdOrderByIdDesc(empresa.getId());
    }

    public List<Viaje> listarPorTransportador(Transportador transportador) {
        return viajeRepository.findByTransportadorIdOrderByIdDesc(transportador.getId());
    }

    // Filtros del admin (null = sin filtro); las fechas son las de recolección
    public List<Viaje> filtrar(List<Viaje> viajes, EstadoViaje estado, Long empresaId, Long transportadorId,
                                LocalDate desde, LocalDate hasta) {
        return viajes.stream()
                .filter(v -> estado == null || v.getEstado() == estado)
                .filter(v -> empresaId == null || v.getCarga().getEmpresa().getId().equals(empresaId))
                .filter(v -> transportadorId == null || v.getTransportador().getId().equals(transportadorId))
                .filter(v -> desde == null || !v.getCarga().getFechaRecoleccion().toLocalDate().isBefore(desde))
                .filter(v -> hasta == null || !v.getCarga().getFechaRecoleccion().toLocalDate().isAfter(hasta))
                .toList();
    }

    public List<Viaje> listarTodos(EstadoViaje estadoFiltro) {
        if (estadoFiltro == null) {
            return viajeRepository.findAllByOrderByIdDesc();
        }
        return viajeRepository.findByEstadoOrderByIdDesc(estadoFiltro);
    }

    public Viaje buscarPorId(Long viajeId) {
        return viajeRepository.findById(viajeId)
                .orElseThrow(() -> new NegocioException("El viaje no existe."));
    }

    public Viaje obtenerPropio(Empresa empresa, Long viajeId) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new NegocioException("El viaje no existe."));
        if (!viaje.getCarga().getEmpresa().getId().equals(empresa.getId())) {
            throw new NegocioException("No tienes permiso sobre este viaje.");
        }
        return viaje;
    }

    public Viaje obtenerPropioTransportador(Transportador transportador, Long viajeId) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new NegocioException("El viaje no existe."));
        if (!viaje.getTransportador().getId().equals(transportador.getId())) {
            throw new NegocioException("No tienes permiso sobre este viaje.");
        }
        return viaje;
    }

    /** Viajes finalizados y cancelados del transportador (se muestran en las ofertas) */
    public record Historial(long finalizados, long canceladosPorEl) {}

    public Historial historialTransportador(Long transportadorId) {
        return new Historial(
                viajeRepository.countByTransportadorIdAndEstado(transportadorId, EstadoViaje.FINALIZADO),
                viajeRepository.countByTransportadorIdAndEstadoAndCanceladoPor(transportadorId, EstadoViaje.CANCELADO,
                        RolUsuario.TRANSPORTADOR));
    }

    @Transactional
    public Viaje iniciar(Transportador transportador, Long viajeId) {
        Viaje viaje = obtenerPropioTransportador(transportador, viajeId);
        if (viaje.getEstado() != EstadoViaje.PROGRAMADO) {
            throw new NegocioException("Solo puedes iniciar viajes que estén programados.");
        }
        viaje.setEstado(EstadoViaje.EN_CURSO);
        viaje.setFechaInicio(LocalDateTime.now());
        Viaje guardado = viajeRepository.save(viaje);
        notificacionService.notificar(viaje.getCarga().getEmpresa().getUsuario(),
                transportador.getNombreCompleto() + " inició el viaje " + ruta(viaje) + ". Puedes verlo en Seguimiento.",
                "/empresa/viajes/" + viaje.getId());
        return guardado;
    }

    /** Marca el viaje como entregado con su prueba de entrega */
    @Transactional
    public Viaje marcarEntregado(Transportador transportador, Long viajeId, MultipartFile prueba) {
        Viaje viaje = obtenerPropioTransportador(transportador, viajeId);
        if (viaje.getEstado() != EstadoViaje.EN_CURSO) {
            throw new NegocioException("Solo puedes marcar como entregado un viaje que esté en curso.");
        }
        String archivo = fileStorageService.guardar(prueba, "viaje_" + viaje.getId() + "_entrega");
        viaje.setEvidenciaRuta(archivo);
        viaje.setEvidenciaNombreOriginal(prueba.getOriginalFilename());
        viaje.setEstado(EstadoViaje.ENTREGADO);
        viaje.setFechaFin(LocalDateTime.now());
        Viaje guardado = viajeRepository.save(viaje);
        notificacionService.notificar(viaje.getCarga().getEmpresa().getUsuario(),
                transportador.getNombreCompleto() + " entregó la carga " + ruta(viaje)
                        + ". Revisa la prueba de entrega y confirma la recepción.",
                "/empresa/viajes/" + viaje.getId());
        return guardado;
    }

    @Transactional
    public Viaje confirmarRecepcion(Empresa empresa, Long viajeId) {
        Viaje viaje = obtenerPropio(empresa, viajeId);
        if (viaje.getEstado() != EstadoViaje.ENTREGADO) {
            throw new NegocioException("Solo puedes confirmar viajes que el transportador haya marcado como entregados.");
        }
        viaje.setEstado(EstadoViaje.FINALIZADO);
        viaje.setComisionPorcentaje(porcentajeComision);
        viaje.setComisionMonto(viaje.getOferta().getMonto()
                .multiply(porcentajeComision)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        viajeRepository.save(viaje);
        cargaService.marcarCompletada(viaje.getCarga());
        notificacionService.notificar(viaje.getTransportador().getUsuario(),
                empresa.getRazonSocial() + " confirmó la recepción de " + ruta(viaje) + ". Recibes "
                        + Textos.dinero(viaje.getNetoTransportador()) + " (descontada la comisión). ¡Ya puedes calificarla!",
                "/transportador/viajes/" + viaje.getId());
        return viaje;
    }

    @Transactional
    public Viaje cancelarPorTransportador(Transportador transportador, Long viajeId, MotivoCancelacion motivo) {
        Viaje viaje = cancelarSiProgramado(obtenerPropioTransportador(transportador, viajeId), motivo, RolUsuario.TRANSPORTADOR);
        notificacionService.notificar(viaje.getCarga().getEmpresa().getUsuario(),
                transportador.getNombreCompleto() + " canceló el viaje " + ruta(viaje)
                        + ". Tu carga volvió a quedar disponible para recibir ofertas.",
                "/empresa/cargas");
        return viaje;
    }

    @Transactional
    public Viaje cancelarPorEmpresa(Empresa empresa, Long viajeId, MotivoCancelacion motivo) {
        Viaje viaje = cancelarSiProgramado(obtenerPropio(empresa, viajeId), motivo, RolUsuario.EMPRESA);
        notificacionService.notificar(viaje.getTransportador().getUsuario(),
                empresa.getRazonSocial() + " canceló el viaje " + ruta(viaje) + ".", "/transportador/viajes");
        return viaje;
    }

    @Transactional
    public Viaje cancelarComoAdmin(Long viajeId) {
        Viaje viaje = buscarPorId(viajeId);
        if (viaje.getEstado() != EstadoViaje.PROGRAMADO && viaje.getEstado() != EstadoViaje.EN_CURSO) {
            throw new NegocioException("Solo puedes cancelar viajes programados o en curso.");
        }
        cancelar(viaje, null, RolUsuario.ADMINISTRADOR);
        String mensaje = "El administrador canceló el viaje " + ruta(viaje) + ".";
        notificacionService.notificar(viaje.getCarga().getEmpresa().getUsuario(), mensaje, "/empresa/viajes");
        notificacionService.notificar(viaje.getTransportador().getUsuario(), mensaje, "/transportador/viajes");
        return viaje;
    }

    // Empresa y transportador solo cancelan viajes programados; uno en curso solo el admin
    private Viaje cancelarSiProgramado(Viaje viaje, MotivoCancelacion motivo, RolUsuario quien) {
        if (viaje.getEstado() != EstadoViaje.PROGRAMADO) {
            throw new NegocioException(
                    "Este viaje ya está en curso: solo un administrador puede cancelarlo a partir de este punto.");
        }
        if (motivo == null) {
            throw new NegocioException("Debes indicar el motivo de la cancelación.");
        }
        return cancelar(viaje, motivo, quien);
    }

    private Viaje cancelar(Viaje viaje, MotivoCancelacion motivo, RolUsuario quien) {
        viaje.setEstado(EstadoViaje.CANCELADO);
        viaje.setMotivoCancelacion(motivo);
        viaje.setCanceladoPor(quien);
        viajeRepository.save(viaje);
        cargaService.reabrirParaOfertas(viaje.getCarga());

        Oferta oferta = viaje.getOferta();
        oferta.setEstado(EstadoOferta.RECHAZADA);
        ofertaRepository.save(oferta);

        return viaje;
    }

    private static String ruta(Viaje viaje) {
        return viaje.getCarga().getOrigen() + " → " + viaje.getCarga().getDestino();
    }

    public Viaje crearDesdeOferta(Oferta ofertaAceptada) {
        if (!ofertaAceptada.getVehiculo().getActivo()) {
            throw new NegocioException("El vehículo de esta oferta ya no está activo.");
        }

        Viaje viaje = new Viaje();
        viaje.setOferta(ofertaAceptada);
        viaje.setCarga(ofertaAceptada.getCarga());
        viaje.setVehiculo(ofertaAceptada.getVehiculo());
        viaje.setTransportador(ofertaAceptada.getTransportador());
        viaje.setEstado(EstadoViaje.PROGRAMADO);
        return viajeRepository.save(viaje);
    }
}
