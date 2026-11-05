package com.booktruck.service;

import com.booktruck.dto.OfertaForm;
import com.booktruck.exception.NegocioException;
import com.booktruck.model.Carga;
import com.booktruck.model.Empresa;
import com.booktruck.model.Oferta;
import com.booktruck.model.Transportador;
import com.booktruck.model.Vehiculo;
import com.booktruck.model.Viaje;
import com.booktruck.model.enums.EstadoCarga;
import com.booktruck.model.enums.EstadoOferta;
import com.booktruck.repository.OfertaRepository;
import com.booktruck.repository.VehiculoRepository;
import com.booktruck.util.Textos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OfertaService {

    private final OfertaRepository ofertaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final CargaService cargaService;
    private final ViajeService viajeService;
    private final VerificacionService verificacionService;
    private final NotificacionService notificacionService;

    public OfertaService(OfertaRepository ofertaRepository, VehiculoRepository vehiculoRepository,
                          CargaService cargaService, ViajeService viajeService, VerificacionService verificacionService,
                          NotificacionService notificacionService) {
        this.ofertaRepository = ofertaRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.cargaService = cargaService;
        this.viajeService = viajeService;
        this.verificacionService = verificacionService;
        this.notificacionService = notificacionService;
    }

    public List<Oferta> listarPorCarga(Carga carga) {
        return ofertaRepository.findByCargaIdOrderByFechaOfertaDesc(carga.getId());
    }

    public List<Oferta> listarPorTransportador(Transportador transportador) {
        return ofertaRepository.findByTransportadorId(transportador.getId());
    }

    public List<Oferta> listarPendientesPorEmpresa(Empresa empresa) {
        return ofertaRepository.findByCarga_EmpresaIdAndEstadoOrderByFechaOfertaDesc(empresa.getId(), EstadoOferta.PENDIENTE);
    }

    public long contarPendientesPorEmpresa(Long empresaId) {
        return ofertaRepository.countByCarga_EmpresaIdAndEstado(empresaId, EstadoOferta.PENDIENTE);
    }

    // Filtros del admin (null = sin filtro)
    public List<Oferta> filtrar(List<Oferta> ofertas, EstadoOferta estado, Long empresaId, Long transportadorId,
                                 BigDecimal montoMinimo, BigDecimal montoMaximo) {
        return ofertas.stream()
                .filter(o -> estado == null || o.getEstado() == estado)
                .filter(o -> empresaId == null || o.getCarga().getEmpresa().getId().equals(empresaId))
                .filter(o -> transportadorId == null || o.getTransportador().getId().equals(transportadorId))
                .filter(o -> montoMinimo == null || o.getMonto().compareTo(montoMinimo) >= 0)
                .filter(o -> montoMaximo == null || o.getMonto().compareTo(montoMaximo) <= 0)
                .toList();
    }

    public List<Oferta> listarTodas(EstadoOferta estadoFiltro) {
        if (estadoFiltro == null) {
            return ofertaRepository.findAllByOrderByFechaOfertaDesc();
        }
        return ofertaRepository.findByEstadoOrderByFechaOfertaDesc(estadoFiltro);
    }

    // Se puede volver a ofertar si la oferta fue rechazada y no llegó a tener viaje
    public boolean puedeVolverAOfertar(Oferta oferta) {
        return oferta.getEstado() == EstadoOferta.RECHAZADA && !viajeService.tieneViajeAsociado(oferta.getId());
    }

    public boolean tieneCapacidadPara(Vehiculo vehiculo, Carga carga) {
        return vehiculo.getCapacidadToneladas().compareTo(carga.getPeso()) >= 0;
    }

    /** Vehículos que pueden ofertar hoy: activos, con licencia, documentos al día y sin viaje activo */
    public List<Vehiculo> vehiculosListos(Transportador transportador) {
        return vehiculoRepository.findByTransportadorId(transportador.getId()).stream()
                .filter(Vehiculo::getActivo)
                .filter(v -> problemaDeLicencia(transportador, v) == null)
                .filter(verificacionService::vehiculoHabilitado)
                .filter(v -> !viajeService.tieneViajeActivo(v.getId()))
                .toList();
    }

    /** Si alguno de los vehículos listos sirve para la carga (capacidad y carrocería). */
    public boolean puedeTomar(List<Vehiculo> listos, Carga carga) {
        return listos.stream().anyMatch(v -> tieneCapacidadPara(v, carga) && carga.admiteCarroceria(v));
    }

    /** Por qué el vehículo no sirve para la carga por su carrocería; null si sirve. */
    public String problemaDeCarroceria(Vehiculo vehiculo, Carga carga) {
        if (carga.admiteCarroceria(vehiculo)) {
            return null;
        }
        return "El vehículo " + vehiculo.getPlaca() + " es " + vehiculo.getTipoCarroceria().getEtiqueta()
                + " y esta carga requiere " + carga.getCarroceriaRequerida().getEtiqueta() + ".";
    }

    /** Por qué la licencia del transportador no le permite usar el vehículo; null si sí puede. */
    public String problemaDeLicencia(Transportador transportador, Vehiculo vehiculo) {
        if (vehiculo.getLicenciaRequerida() == null) {
            return "Al vehículo " + vehiculo.getPlaca() + " le falta el peso bruto vehicular. Complétalo en Vehículos para poder ofertar con él.";
        }
        if (!transportador.puedeConducir(vehiculo)) {
            return "El vehículo " + vehiculo.getPlaca() + " requiere licencia " + vehiculo.getLicenciaRequerida().name()
                    + " y la tuya es " + transportador.getLicencia() + ".";
        }
        return null;
    }

    @Transactional
    public Oferta crear(Transportador transportador, Carga carga, OfertaForm form) {
        if (!verificacionService.transportadorVerificado(transportador)) {
            throw new NegocioException("Tu cuenta aún no está verificada. Sube tus documentos en tu perfil y espera "
                    + "la aprobación del administrador para poder ofertar.");
        }
        if (carga.getEstado() != EstadoCarga.PUBLICADA) {
            throw new NegocioException("Esta carga ya no está disponible para recibir ofertas.");
        }
        if (carga.isRecoleccionPasada()) {
            throw new NegocioException("Ya pasó la fecha de recolección de esta carga; no recibe más ofertas.");
        }
        if (form.getMonto() == null || form.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("El monto ofrecido debe ser mayor a $0.");
        }
        if (form.getVehiculoId() == null) {
            throw new NegocioException("Debes seleccionar un vehículo.");
        }
        // Hay una sola oferta por carga y transportador; si la anterior fue rechazada se reutiliza
        Optional<Oferta> anterior = ofertaRepository.findByCargaIdAndTransportadorId(carga.getId(), transportador.getId());
        if (anterior.isPresent() && !puedeVolverAOfertar(anterior.get())) {
            throw new NegocioException(anterior.get().getEstado() == EstadoOferta.RECHAZADA
                    ? "Ya tuviste un viaje cancelado con esta carga, por eso no puedes volver a ofertar en ella."
                    : "Ya tienes una oferta registrada para esta carga.");
        }

        Vehiculo vehiculo = vehiculoRepository.findById(form.getVehiculoId())
                .orElseThrow(() -> new NegocioException("El vehículo seleccionado no existe."));
        if (!vehiculo.getTransportador().getId().equals(transportador.getId())) {
            throw new NegocioException("No tienes permiso sobre ese vehículo.");
        }
        if (!vehiculo.getActivo()) {
            throw new NegocioException("El vehículo seleccionado no está activo.");
        }
        if (!tieneCapacidadPara(vehiculo, carga)) {
            throw new NegocioException("El vehículo " + vehiculo.getPlaca() + " ("
                    + vehiculo.getCapacidadToneladas().stripTrailingZeros().toPlainString()
                    + " ton) no tiene capacidad para esta carga ("
                    + carga.getPeso().stripTrailingZeros().toPlainString() + " ton).");
        }
        String problemaCarroceria = problemaDeCarroceria(vehiculo, carga);
        if (problemaCarroceria != null) {
            throw new NegocioException(problemaCarroceria);
        }
        String problemaLicencia = problemaDeLicencia(transportador, vehiculo);
        if (problemaLicencia != null) {
            throw new NegocioException(problemaLicencia);
        }
        if (!verificacionService.vehiculoHabilitado(vehiculo)) {
            throw new NegocioException("Los documentos del vehículo " + vehiculo.getPlaca() + " no están al día: "
                    + "licencia de tránsito, SOAT y técnico-mecánica deben estar aprobados y vigentes.");
        }
        if (viajeService.tieneViajeActivo(vehiculo.getId())) {
            throw new NegocioException("Ese vehículo ya tiene un viaje programado o en curso. Un vehículo solo puede llevar un viaje a la vez.");
        }

        Oferta oferta = anterior.orElseGet(Oferta::new);
        oferta.setCarga(carga);
        oferta.setTransportador(transportador);
        oferta.setVehiculo(vehiculo);
        oferta.setMonto(form.getMonto());
        oferta.setEstado(EstadoOferta.PENDIENTE);
        if (anterior.isPresent()) {
            oferta.setFechaOferta(LocalDateTime.now());
        }
        Oferta guardada = ofertaRepository.save(oferta);
        notificacionService.notificar(carga.getEmpresa().getUsuario(),
                "Nueva oferta de " + transportador.getNombreCompleto() + " por " + Textos.dinero(form.getMonto())
                        + " en tu carga " + carga.getOrigen() + " → " + carga.getDestino() + ".",
                "/empresa/cargas/" + carga.getId() + "/ofertas");
        return guardada;
    }

    @Transactional
    public void retirarPorCarga(Transportador transportador, Long cargaId) {
        Oferta oferta = ofertaRepository.findByCargaIdAndTransportadorId(cargaId, transportador.getId())
                .orElseThrow(() -> new NegocioException("No tienes una oferta registrada para esta carga."));
        if (oferta.getEstado() != EstadoOferta.PENDIENTE) {
            throw new NegocioException("Solo puedes retirar ofertas que estén pendientes.");
        }
        oferta.setEstado(EstadoOferta.RECHAZADA);
        ofertaRepository.save(oferta);
    }

    @Transactional
    public Viaje seleccionar(Empresa empresa, Long cargaId, Long ofertaId) {
        if (!verificacionService.empresaVerificada(empresa)) {
            throw new NegocioException("Tu cuenta aún no está verificada. Sube tus documentos en tu perfil y espera "
                    + "la aprobación del administrador para poder aceptar ofertas.");
        }
        Carga carga = cargaService.obtenerPropia(empresa, cargaId);
        if (carga.getEstado() != EstadoCarga.PUBLICADA) {
            throw new NegocioException("Esta carga ya no está disponible para asignar.");
        }
        if (carga.isRecoleccionPasada()) {
            throw new NegocioException("Ya pasó la fecha de recolección de esta carga. Edítala con una fecha nueva "
                    + "para volver a publicarla.");
        }

        List<Oferta> ofertasCarga = ofertaRepository.findByCargaIdOrderByFechaOfertaDesc(carga.getId());
        Oferta ofertaSeleccionada = ofertasCarga.stream()
                .filter(o -> o.getId().equals(ofertaId))
                .findFirst()
                .orElseThrow(() -> new NegocioException("La oferta no corresponde a esta carga."));
        if (ofertaSeleccionada.getEstado() != EstadoOferta.PENDIENTE) {
            throw new NegocioException("Esta oferta ya fue procesada.");
        }
        if (problemaDeCarroceria(ofertaSeleccionada.getVehiculo(), carga) != null) {
            throw new NegocioException("No puedes aceptar esta oferta: el vehículo " + ofertaSeleccionada.getVehiculo().getPlaca()
                    + " no tiene la carrocería que requiere la carga.");
        }
        // Los documentos pudieron vencerse entre la oferta y su aceptación
        if (!verificacionService.transportadorVerificado(ofertaSeleccionada.getTransportador())
                || !verificacionService.vehiculoHabilitado(ofertaSeleccionada.getVehiculo())
                || problemaDeLicencia(ofertaSeleccionada.getTransportador(), ofertaSeleccionada.getVehiculo()) != null) {
            throw new NegocioException("No puedes aceptar esta oferta: el transportador o su vehículo ya no tienen "
                    + "los documentos al día. Elige otra oferta o espera a que los renueve.");
        }

        String ruta = carga.getOrigen() + " → " + carga.getDestino();
        for (Oferta oferta : ofertasCarga) {
            if (oferta.getId().equals(ofertaSeleccionada.getId())) {
                oferta.setEstado(EstadoOferta.ACEPTADA);
                notificacionService.notificar(oferta.getTransportador().getUsuario(),
                        "¡Aceptaron tu oferta de " + Textos.dinero(oferta.getMonto()) + " en la carga " + ruta
                                + "! Ya tienes un viaje programado.",
                        "/transportador/viajes");
            } else if (oferta.getEstado() == EstadoOferta.PENDIENTE) {
                oferta.setEstado(EstadoOferta.RECHAZADA);
                notificacionService.notificar(oferta.getTransportador().getUsuario(),
                        "La empresa eligió otra oferta para la carga " + ruta + ".", "/transportador/ofertas");
            }
            ofertaRepository.save(oferta);
        }

        cargaService.marcarAsignada(carga);

        return viajeService.crearDesdeOferta(ofertaSeleccionada);
    }
}
