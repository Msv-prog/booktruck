package com.booktruck.service;

import com.booktruck.dto.CargaForm;
import com.booktruck.exception.NegocioException;
import com.booktruck.model.Carga;
import com.booktruck.model.Empresa;
import com.booktruck.model.Oferta;
import com.booktruck.model.enums.EstadoCarga;
import com.booktruck.model.enums.EstadoOferta;
import com.booktruck.model.enums.TipoCarroceria;
import com.booktruck.repository.CargaRepository;
import com.booktruck.repository.OfertaRepository;
import com.booktruck.util.Textos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CargaService {

    private final CargaRepository cargaRepository;
    private final OfertaRepository ofertaRepository;
    private final VerificacionService verificacionService;
    private final NotificacionService notificacionService;

    public CargaService(CargaRepository cargaRepository, OfertaRepository ofertaRepository,
                        VerificacionService verificacionService, NotificacionService notificacionService) {
        this.cargaRepository = cargaRepository;
        this.ofertaRepository = ofertaRepository;
        this.verificacionService = verificacionService;
        this.notificacionService = notificacionService;
    }

    public List<Carga> listarPorEmpresa(Empresa empresa, EstadoCarga estadoFiltro) {
        if (estadoFiltro == null) {
            return cargaRepository.findByEmpresaIdOrderByFechaPublicacionDesc(empresa.getId());
        }
        return cargaRepository.findByEmpresaIdAndEstadoOrderByFechaPublicacionDesc(empresa.getId(), estadoFiltro);
    }

    public List<Carga> listarPublicadasDisponibles(String origen, String destino, String tipoMercancia,
                                                    BigDecimal pesoMaximo, BigDecimal precioMinimo, BigDecimal precioMaximo) {
        String origenFiltro = (origen == null || origen.isBlank()) ? null : origen.trim();
        String destinoFiltro = (destino == null || destino.isBlank()) ? null : destino.trim();
        String tipoMercanciaFiltro = (tipoMercancia == null || tipoMercancia.isBlank()) ? null : tipoMercancia.trim();
        // Oculta las cargas con fecha pasada aunque la tarea todavía no las haya marcado como vencidas
        return cargaRepository.buscarDisponiblesConFiltros(EstadoCarga.PUBLICADA, origenFiltro, destinoFiltro,
                tipoMercanciaFiltro, pesoMaximo, precioMinimo, precioMaximo).stream()
                .filter(c -> !c.isRecoleccionPasada())
                .toList();
    }

    public long contarPorEmpresa(Long empresaId) {
        return cargaRepository.countByEmpresaId(empresaId);
    }

    // Filtros del admin (null o vacío = sin filtro)
    public List<Carga> filtrar(List<Carga> cargas, EstadoCarga estado, String origen, String destino,
                                String tipoMercancia, Long empresaId, LocalDate desde, LocalDate hasta,
                                TipoCarroceria carroceria) {
        return cargas.stream()
                .filter(c -> estado == null || c.getEstado() == estado)
                .filter(c -> carroceria == null || c.getCarroceriaRequerida() == carroceria)
                .filter(c -> Textos.vacio(origen) || origen.equals(c.getOrigen()))
                .filter(c -> Textos.vacio(destino) || destino.equals(c.getDestino()))
                .filter(c -> Textos.vacio(tipoMercancia) || tipoMercancia.equals(c.getTipoMercancia()))
                .filter(c -> empresaId == null || c.getEmpresa().getId().equals(empresaId))
                .filter(c -> desde == null || !c.getFechaPublicacion().toLocalDate().isBefore(desde))
                .filter(c -> hasta == null || !c.getFechaPublicacion().toLocalDate().isAfter(hasta))
                .toList();
    }

    public List<Carga> listarTodas(EstadoCarga estadoFiltro) {
        if (estadoFiltro == null) {
            return cargaRepository.findAllByOrderByFechaPublicacionDesc();
        }
        return cargaRepository.findByEstadoOrderByFechaPublicacionDesc(estadoFiltro);
    }

    public Carga buscarPorId(Long cargaId) {
        return cargaRepository.findById(cargaId)
                .orElseThrow(() -> new NegocioException("La carga no existe."));
    }

    public Carga obtenerPropia(Empresa empresa, Long cargaId) {
        Carga carga = cargaRepository.findById(cargaId)
                .orElseThrow(() -> new NegocioException("La carga no existe."));
        if (!carga.getEmpresa().getId().equals(empresa.getId())) {
            throw new NegocioException("No tienes permiso sobre esta carga.");
        }
        return carga;
    }

    @Transactional
    public void marcarAsignada(Carga carga) {
        carga.setEstado(EstadoCarga.ASIGNADA);
        cargaRepository.save(carga);
    }

    @Transactional
    public void marcarCompletada(Carga carga) {
        carga.setEstado(EstadoCarga.COMPLETADA);
        cargaRepository.save(carga);
    }

    // Tras cancelar un viaje: si la fecha de recolección ya pasó, queda vencida en vez de publicada
    @Transactional
    public void reabrirParaOfertas(Carga carga) {
        carga.setEstado(carga.isRecoleccionPasada() ? EstadoCarga.VENCIDA : EstadoCarga.PUBLICADA);
        cargaRepository.save(carga);
    }

    @Transactional
    public void cancelar(Empresa empresa, Long cargaId) {
        Carga carga = obtenerPropia(empresa, cargaId);
        if (carga.getEstado() != EstadoCarga.PUBLICADA && carga.getEstado() != EstadoCarga.VENCIDA) {
            throw new NegocioException("Solo puedes cancelar cargas publicadas o vencidas.");
        }

        carga.setEstado(EstadoCarga.CANCELADA);
        cargaRepository.save(carga);
        cerrarOfertasPendientes(carga, "La empresa canceló la carga " + ruta(carga) + "; tu oferta quedó cerrada.");
    }

    /** Pasa a VENCIDA las cargas cuya fecha ya pasó y cierra sus ofertas (lo llama TareasProgramadas) */
    @Transactional
    public int vencerCargasPasadas() {
        List<Carga> pasadas = cargaRepository.findByEstadoAndFechaRecoleccionBefore(EstadoCarga.PUBLICADA, LocalDateTime.now());
        for (Carga carga : pasadas) {
            carga.setEstado(EstadoCarga.VENCIDA);
            cargaRepository.save(carga);
            cerrarOfertasPendientes(carga, "La carga " + ruta(carga) + " venció sin ser asignada; tu oferta quedó cerrada.");
            notificacionService.notificar(carga.getEmpresa().getUsuario(),
                    "Tu carga " + ruta(carga) + " venció: llegó la fecha de recolección y nadie la tomó. "
                            + "Puedes editarla con otra fecha para volver a publicarla.",
                    "/empresa/cargas?estado=VENCIDA");
        }
        return pasadas.size();
    }

    // Rechaza las ofertas pendientes de la carga y avisa a cada transportador
    private int cerrarOfertasPendientes(Carga carga, String mensaje) {
        List<Oferta> pendientes = ofertaRepository.findByCargaIdAndEstado(carga.getId(), EstadoOferta.PENDIENTE);
        for (Oferta oferta : pendientes) {
            oferta.setEstado(EstadoOferta.RECHAZADA);
            ofertaRepository.save(oferta);
            notificacionService.notificar(oferta.getTransportador().getUsuario(), mensaje, "/transportador/ofertas");
        }
        return pendientes.size();
    }

    private static String ruta(Carga carga) {
        return carga.getOrigen() + " → " + carga.getDestino();
    }

    @Transactional
    public Carga publicar(Empresa empresa, CargaForm form) {
        validar(empresa, form);

        Carga carga = new Carga();
        carga.setEmpresa(empresa);
        aplicar(carga, form);
        carga.setEstado(EstadoCarga.PUBLICADA);
        return cargaRepository.save(carga);
    }

    /** Solo se editan cargas publicadas o vencidas */
    public boolean esEditable(Carga carga) {
        return carga.getEstado() == EstadoCarga.PUBLICADA || carga.getEstado() == EstadoCarga.VENCIDA;
    }

    /** Edita la carga; si cambia ruta, mercancía, peso, carrocería o fecha se cierran las ofertas pendientes */
    @Transactional
    public int editar(Empresa empresa, Long cargaId, CargaForm form) {
        Carga carga = obtenerPropia(empresa, cargaId);
        if (!esEditable(carga)) {
            throw new NegocioException("Solo puedes editar cargas publicadas o vencidas. Esta carga ya está "
                    + carga.getEstado().name().toLowerCase().replace('_', ' ') + ".");
        }
        validar(empresa, form);

        boolean cambioImportante = !form.getOrigen().equals(carga.getOrigen())
                || !form.getDestino().equals(carga.getDestino())
                || !form.getTipoMercancia().equals(carga.getTipoMercancia())
                || form.getPeso().compareTo(carga.getPeso()) != 0
                || form.getCarroceriaRequerida() != carga.getCarroceriaRequerida()
                || !form.getFechaRecoleccion().equals(carga.getFechaRecoleccion());

        aplicar(carga, form);
        carga.setEstado(EstadoCarga.PUBLICADA);
        cargaRepository.save(carga);

        if (!cambioImportante) {
            return 0;
        }
        return cerrarOfertasPendientes(carga, "La empresa cambió los datos de la carga " + ruta(carga)
                + "; tu oferta quedó cerrada. Puedes volver a ofertar con los datos nuevos.");
    }

    private void aplicar(Carga carga, CargaForm form) {
        carga.setOrigen(form.getOrigen());
        carga.setDestino(form.getDestino());
        carga.setDireccionOrigen(form.getDireccionOrigen());
        carga.setDireccionDestino(form.getDireccionDestino());
        carga.setTipoMercancia(form.getTipoMercancia());
        carga.setPeso(form.getPeso());
        carga.setPrecioOfrecido(form.getPrecioOfrecido());
        carga.setFechaRecoleccion(form.getFechaRecoleccion());
        carga.setCarroceriaRequerida(form.getCarroceriaRequerida());
    }

    private void validar(Empresa empresa, CargaForm form) {
        if (!verificacionService.empresaVerificada(empresa)) {
            throw new NegocioException("Tu cuenta aún no está verificada. Sube tus documentos en tu perfil y espera "
                    + "la aprobación del administrador para poder publicar cargas.");
        }
        if (form.getOrigen() == null || form.getOrigen().isBlank()) {
            throw new NegocioException("El origen es obligatorio.");
        }
        if (form.getDestino() == null || form.getDestino().isBlank()) {
            throw new NegocioException("El destino es obligatorio.");
        }
        if (form.getDireccionOrigen() == null || form.getDireccionOrigen().isBlank()) {
            throw new NegocioException("La dirección de origen es obligatoria.");
        }
        if (form.getDireccionDestino() == null || form.getDireccionDestino().isBlank()) {
            throw new NegocioException("La dirección de destino es obligatoria.");
        }
        if (form.getTipoMercancia() == null || form.getTipoMercancia().isBlank()) {
            throw new NegocioException("El tipo de mercancía es obligatorio.");
        }
        if (form.getPeso() == null || form.getPeso().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("El peso debe ser mayor que cero.");
        }
        if (form.getPrecioOfrecido() == null || form.getPrecioOfrecido().compareTo(BigDecimal.ZERO) < 0) {
            throw new NegocioException("El precio ofrecido no puede ser negativo.");
        }
        if (form.getOrigen().trim().equalsIgnoreCase(form.getDestino().trim())) {
            throw new NegocioException("El origen y el destino deben ser diferentes.");
        }
        if (form.getFechaRecoleccion() == null) {
            throw new NegocioException("La fecha de recolección es obligatoria.");
        }
        if (form.getFechaRecoleccion().isBefore(LocalDateTime.now())) {
            throw new NegocioException("La fecha de recolección debe ser posterior a la fecha actual.");
        }
    }
}
