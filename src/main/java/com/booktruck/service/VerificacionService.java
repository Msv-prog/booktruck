package com.booktruck.service;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.Documento;
import com.booktruck.model.Empresa;
import com.booktruck.model.Transportador;
import com.booktruck.model.Vehiculo;
import com.booktruck.model.enums.DuenoDocumento;
import com.booktruck.model.enums.EstadoVerificacion;
import com.booktruck.model.enums.TipoDocumento;
import com.booktruck.repository.DocumentoRepository;
import com.booktruck.repository.EmpresaRepository;
import com.booktruck.repository.TransportadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Documentos de verificación: subida, revisión y estado de cada cuenta o vehículo */
@Service
public class VerificacionService {

    private static final int LARGO_MAXIMO_MOTIVO = 255;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DocumentoRepository documentoRepository;
    private final FileStorageService fileStorageService;
    private final EmpresaRepository empresaRepository;
    private final TransportadorRepository transportadorRepository;
    private final NotificacionService notificacionService;

    public VerificacionService(DocumentoRepository documentoRepository, FileStorageService fileStorageService,
                               EmpresaRepository empresaRepository, TransportadorRepository transportadorRepository,
                               NotificacionService notificacionService) {
        this.documentoRepository = documentoRepository;
        this.fileStorageService = fileStorageService;
        this.empresaRepository = empresaRepository;
        this.transportadorRepository = transportadorRepository;
        this.notificacionService = notificacionService;
    }

    // --- Contadores del admin ---

    public long contarDocumentosPorRevisar() {
        return documentoRepository.countByEstado(EstadoVerificacion.PENDIENTE);
    }

    public long contarEmpresasPorRevisar() {
        return documentoRepository.contarEmpresasConDocumentoEn(EstadoVerificacion.PENDIENTE);
    }

    public long contarTransportadoresPorRevisar() {
        return documentoRepository.contarTransportadoresConDocumentoEn(EstadoVerificacion.PENDIENTE);
    }

    public long contarVehiculosPorRevisar() {
        return documentoRepository.contarVehiculosConDocumentoEn(EstadoVerificacion.PENDIENTE);
    }

    // --- Consultas ---

    public List<Documento> documentosDe(Empresa empresa) {
        return documentoRepository.findByEmpresaId(empresa.getId());
    }

    public List<Documento> documentosDe(Transportador transportador) {
        return documentoRepository.findByTransportadorId(transportador.getId());
    }

    public List<Documento> documentosDe(Vehiculo vehiculo) {
        return documentoRepository.findByVehiculoId(vehiculo.getId());
    }

    public Optional<Documento> documentoDe(Empresa empresa, TipoDocumento tipo) {
        return documentoRepository.findByEmpresaIdAndTipo(empresa.getId(), tipo);
    }

    public Optional<Documento> documentoDe(Transportador transportador, TipoDocumento tipo) {
        return documentoRepository.findByTransportadorIdAndTipo(transportador.getId(), tipo);
    }

    public Optional<Documento> documentoDe(Vehiculo vehiculo, TipoDocumento tipo) {
        return documentoRepository.findByVehiculoIdAndTipo(vehiculo.getId(), tipo);
    }

    public Documento buscarPorId(Long documentoId) {
        return documentoRepository.findById(documentoId)
                .orElseThrow(() -> new NegocioException("El documento no existe."));
    }

    /** Documentos indexados por tipo, para pintar una tarjeta por cada tipo requerido. */
    public Map<TipoDocumento, Documento> porTipo(List<Documento> documentos) {
        Map<TipoDocumento, Documento> mapa = new EnumMap<>(TipoDocumento.class);
        documentos.forEach(d -> mapa.put(d.getTipo(), d));
        return mapa;
    }

    // --- Reglas ---

    /** RECHAZADO si hay uno rechazado, APROBADO si todos están aprobados y vigentes, si no PENDIENTE */
    public EstadoVerificacion estadoCalculado(DuenoDocumento dueno, List<Documento> documentos) {
        Map<TipoDocumento, Documento> mapa = porTipo(documentos);
        List<TipoDocumento> obligatorios = TipoDocumento.de(dueno).stream().filter(TipoDocumento::isObligatorio).toList();

        boolean algunoRechazado = obligatorios.stream()
                .map(mapa::get)
                .anyMatch(d -> d != null && d.getEstado() == EstadoVerificacion.RECHAZADO);
        if (algunoRechazado) {
            return EstadoVerificacion.RECHAZADO;
        }
        boolean todosVigentes = obligatorios.stream()
                .map(mapa::get)
                .allMatch(d -> d != null && d.isVigente());
        return todosVigentes ? EstadoVerificacion.APROBADO : EstadoVerificacion.PENDIENTE;
    }

    // Se calcula cada vez, así un documento vencido bloquea el mismo día

    /** Avance de la verificación: cuántos documentos obligatorios están aprobados y vigentes. */
    public static class Progreso {
        private final int aprobados;
        private final int total;

        public Progreso(int aprobados, int total) {
            this.aprobados = aprobados;
            this.total = total;
        }

        public int getAprobados() { return aprobados; }
        public int getTotal() { return total; }
        public int getPorcentaje() { return total == 0 ? 0 : aprobados * 100 / total; }
        public boolean isCompleto() { return total > 0 && aprobados == total; }
    }

    public Progreso progreso(DuenoDocumento dueno, List<Documento> documentos) {
        Map<TipoDocumento, Documento> mapa = porTipo(documentos);
        List<TipoDocumento> obligatorios = TipoDocumento.de(dueno).stream().filter(TipoDocumento::isObligatorio).toList();
        int aprobados = (int) obligatorios.stream().map(mapa::get).filter(d -> d != null && d.isVigente()).count();
        return new Progreso(aprobados, obligatorios.size());
    }

    public EstadoVerificacion estadoEmpresa(Empresa empresa) {
        return estadoCalculado(DuenoDocumento.EMPRESA, documentosDe(empresa));
    }

    public EstadoVerificacion estadoTransportador(Transportador transportador) {
        return estadoCalculado(DuenoDocumento.TRANSPORTADOR, documentosDe(transportador));
    }

    public EstadoVerificacion estadoVehiculo(Vehiculo vehiculo) {
        return estadoCalculado(DuenoDocumento.VEHICULO, documentosDe(vehiculo));
    }

    public boolean empresaVerificada(Empresa empresa) {
        return estadoEmpresa(empresa) == EstadoVerificacion.APROBADO;
    }

    public boolean transportadorVerificado(Transportador transportador) {
        return estadoTransportador(transportador) == EstadoVerificacion.APROBADO;
    }

    /** Un vehículo solo se puede usar para ofertar si sus documentos obligatorios están aprobados y vigentes. */
    public boolean vehiculoHabilitado(Vehiculo vehiculo) {
        return estadoVehiculo(vehiculo) == EstadoVerificacion.APROBADO;
    }

    /** Documentos aprobados que ya vencieron o vencen en los próximos días (para el aviso del admin). */
    public List<Documento> proximosAVencer() {
        return documentoRepository.findByEstadoAndFechaVencimientoLessThanEqualOrderByFechaVencimiento(
                EstadoVerificacion.APROBADO, LocalDate.now().plusDays(Documento.DIAS_AVISO_VENCIMIENTO));
    }

    // --- Aviso fijo de la cuenta ---

    /** Aviso de la parte de arriba: nivel "error" (bloquea) o "advertencia"; null si no hay aviso */
    public record Aviso(String nivel, String titulo, List<String> lineas, String enlaceTexto, String enlaceUrl) {}

    public Aviso avisoEmpresa(Empresa empresa) {
        return avisoCuenta(DuenoDocumento.EMPRESA, documentosDe(empresa), "/empresa/perfil#documentos",
                "publicar cargas ni aceptar ofertas", "publicar cargas y aceptar ofertas", List.of());
    }

    public Aviso avisoTransportador(Transportador transportador, List<Vehiculo> vehiculos) {
        List<String> lineasVehiculos = new ArrayList<>();
        for (Vehiculo vehiculo : vehiculos) {
            if (!vehiculo.getActivo()) {
                continue;
            }
            if (vehiculo.getLicenciaRequerida() == null) {
                lineasVehiculos.add("Vehículo " + vehiculo.getPlaca() + ": falta el peso bruto vehicular. No podrás ofertar con él.");
                continue;
            }
            if (!transportador.puedeConducir(vehiculo)) {
                lineasVehiculos.add("Vehículo " + vehiculo.getPlaca() + ": requiere licencia " + vehiculo.getLicenciaRequerida().name()
                        + " y la tuya es " + transportador.getLicencia() + ". No podrás ofertar con él.");
                continue;
            }
            List<Documento> documentos = documentosDe(vehiculo);
            String problema = describirProblema(DuenoDocumento.VEHICULO, documentos);
            if (problema != null) {
                lineasVehiculos.add("Vehículo " + vehiculo.getPlaca() + ": " + problema + " No podrás ofertar con él.");
            } else {
                documentos.stream().filter(Documento::isPorVencer).forEach(d -> lineasVehiculos.add(
                        "Vehículo " + vehiculo.getPlaca() + ": " + porVencer(d)));
            }
        }
        return avisoCuenta(DuenoDocumento.TRANSPORTADOR, documentosDe(transportador), "/transportador/perfil#documentos",
                "ofertar en las cargas disponibles", "ofertar en las cargas disponibles", lineasVehiculos);
    }

    private Aviso avisoCuenta(DuenoDocumento dueno, List<Documento> documentos, String urlPerfil,
                              String accionesNo, String accionesSi, List<String> lineasExtra) {
        Map<TipoDocumento, Documento> mapa = porTipo(documentos);
        List<TipoDocumento> obligatorios = TipoDocumento.de(dueno).stream().filter(TipoDocumento::isObligatorio).toList();

        List<String> rechazados = obligatorios.stream().map(mapa::get)
                .filter(d -> d != null && d.getEstado() == EstadoVerificacion.RECHAZADO)
                .map(d -> d.getTipo().getEtiqueta() + ": " + d.getMotivoRechazo()).toList();
        if (!rechazados.isEmpty()) {
            return new Aviso("error", "Tienes documentos rechazados",
                    conExtra(rechazados, "Sube uno nuevo de cada uno; mientras tanto no podrás " + accionesNo + ".", lineasExtra),
                    "Subir documentos", urlPerfil);
        }
        List<Documento> docsVencidos = obligatorios.stream().map(mapa::get)
                .filter(d -> d != null && d.getEstado() == EstadoVerificacion.APROBADO && d.isVencido()).toList();
        List<String> vencidos = docsVencidos.stream()
                .map(d -> d.getTipo().getEtiqueta() + " venció el " + d.getFechaVencimiento().format(FORMATO_FECHA) + ".").toList();
        if (!vencidos.isEmpty()) {
            String renovar = docsVencidos.size() == 1
                    ? "Sube " + docsVencidos.get(0).getTipo().concordar("un") + " vigente"
                    : "Sube uno vigente de cada uno";
            return new Aviso("error", "Tienes documentos vencidos",
                    conExtra(vencidos, renovar + "; mientras tanto no podrás " + accionesNo + ".", lineasExtra),
                    "Renovar documentos", urlPerfil);
        }
        List<String> faltantes = obligatorios.stream().filter(t -> !mapa.containsKey(t)).map(TipoDocumento::getEtiqueta).toList();
        if (!faltantes.isEmpty()) {
            return new Aviso("advertencia", "Verifica tu cuenta",
                    conExtra(List.of("Te falta subir: " + String.join(", ", faltantes) + "."),
                            "Mientras tanto no podrás " + accionesNo + ".", lineasExtra),
                    "Subir documentos", urlPerfil);
        }
        List<String> enRevision = obligatorios.stream().map(mapa::get)
                .filter(d -> d.getEstado() == EstadoVerificacion.PENDIENTE).map(d -> d.getTipo().getEtiqueta()).toList();
        if (!enRevision.isEmpty()) {
            return new Aviso("advertencia", "Tus documentos están en revisión",
                    conExtra(List.of("El administrador está revisando: " + String.join(", ", enRevision) + "."),
                            "Cuando los apruebe podrás " + accionesSi + ".", lineasExtra),
                    null, null);
        }
        // Cuenta verificada: solo se avisa de vencimientos próximos o de vehículos con problemas
        List<String> lineas = new ArrayList<>(obligatorios.stream().map(mapa::get)
                .filter(Documento::isPorVencer).map(this::porVencer).toList());
        lineas.addAll(lineasExtra);
        if (lineas.isEmpty()) {
            return null;
        }
        boolean hayVehiculos = !lineasExtra.isEmpty();
        return new Aviso("advertencia", "Revisa tus documentos", lineas,
                hayVehiculos ? "Ver vehículos" : "Renovar documentos",
                hayVehiculos ? "/transportador/vehiculos" : urlPerfil);
    }

    /** Qué le impide a un dueño estar al día, en una frase; null si todo está aprobado y vigente. */
    private String describirProblema(DuenoDocumento dueno, List<Documento> documentos) {
        Map<TipoDocumento, Documento> mapa = porTipo(documentos);
        List<String> partes = new ArrayList<>();
        for (TipoDocumento tipo : TipoDocumento.de(dueno)) {
            if (!tipo.isObligatorio()) {
                continue;
            }
            Documento doc = mapa.get(tipo);
            if (doc == null) {
                partes.add(tipo.getEtiqueta() + " sin subir");
            } else if (doc.getEstado() == EstadoVerificacion.RECHAZADO) {
                partes.add(tipo.getEtiqueta() + " " + tipo.concordar("rechazad"));
            } else if (doc.isVencido()) {
                partes.add(tipo.getEtiqueta() + " " + tipo.concordar("vencid"));
            } else if (doc.getEstado() == EstadoVerificacion.PENDIENTE) {
                partes.add(tipo.getEtiqueta() + " en revisión");
            }
        }
        return partes.isEmpty() ? null : String.join(", ", partes) + ".";
    }

    private String porVencer(Documento documento) {
        return documento.getTipo().getEtiqueta() + " vence en " + documento.getDiasParaVencer() + " días ("
                + documento.getFechaVencimiento().format(FORMATO_FECHA) + "). "
                + documento.getTipo().concordar("Súbel") + " " + documento.getTipo().concordar("renovad") + " a tiempo.";
    }

    private static List<String> conExtra(List<String> lineas, String cierre, List<String> extra) {
        List<String> resultado = new ArrayList<>(lineas);
        resultado.add(cierre);
        resultado.addAll(extra);
        return resultado;
    }

    // --- Subida ---

    @Transactional
    public Documento subir(Empresa empresa, TipoDocumento tipo, MultipartFile archivo, LocalDate fechaVencimiento) {
        validarTipo(tipo, DuenoDocumento.EMPRESA);
        Documento documento = documentoRepository.findByEmpresaIdAndTipo(empresa.getId(), tipo).orElseGet(Documento::new);
        documento.setEmpresa(empresa);
        return guardar(documento, tipo, archivo, fechaVencimiento, "empresa_" + empresa.getId());
    }

    @Transactional
    public Documento subir(Transportador transportador, TipoDocumento tipo, MultipartFile archivo, LocalDate fechaVencimiento) {
        validarTipo(tipo, DuenoDocumento.TRANSPORTADOR);
        Documento documento = documentoRepository.findByTransportadorIdAndTipo(transportador.getId(), tipo).orElseGet(Documento::new);
        documento.setTransportador(transportador);
        return guardar(documento, tipo, archivo, fechaVencimiento, "transportador_" + transportador.getId());
    }

    @Transactional
    public Documento subir(Vehiculo vehiculo, TipoDocumento tipo, MultipartFile archivo, LocalDate fechaVencimiento) {
        validarTipo(tipo, DuenoDocumento.VEHICULO);
        Documento documento = documentoRepository.findByVehiculoIdAndTipo(vehiculo.getId(), tipo).orElseGet(Documento::new);
        documento.setVehiculo(vehiculo);
        return guardar(documento, tipo, archivo, fechaVencimiento, "vehiculo_" + vehiculo.getId());
    }

    private void validarTipo(TipoDocumento tipo, DuenoDocumento dueno) {
        if (tipo == null || tipo.getDueno() != dueno) {
            throw new NegocioException("Ese tipo de documento no corresponde.");
        }
    }

    // Reemplaza archivo y datos; el documento vuelve a quedar pendiente de revisión
    private Documento guardar(Documento documento, TipoDocumento tipo, MultipartFile archivo,
                              LocalDate fechaVencimiento, String prefijo) {
        if (tipo.isVence()) {
            if (fechaVencimiento == null) {
                throw new NegocioException("Indica la fecha de vencimiento del documento.");
            }
            if (fechaVencimiento.isBefore(LocalDate.now())) {
                throw new NegocioException("Ese documento ya está vencido. Sube uno vigente.");
            }
        }

        String archivoAnterior = documento.getArchivoRuta();
        String nuevoArchivo = fileStorageService.guardar(archivo, prefijo + "_" + tipo.name().toLowerCase());

        documento.setTipo(tipo);
        documento.setArchivoRuta(nuevoArchivo);
        documento.setNombreOriginal(archivo.getOriginalFilename());
        documento.setFechaVencimiento(tipo.isVence() ? fechaVencimiento : null);
        documento.setEstado(EstadoVerificacion.PENDIENTE);
        documento.setMotivoRechazo(null);
        documento.setFechaSubida(LocalDateTime.now());
        documento.setFechaRevision(null);
        Documento guardado = documentoRepository.save(documento);
        actualizarEstadoCuenta(guardado);

        fileStorageService.eliminar(archivoAnterior);
        return guardado;
    }

    // --- Revisión del administrador ---

    @Transactional
    public Documento aprobar(Long documentoId) {
        Documento documento = buscarPorId(documentoId);
        documento.setEstado(EstadoVerificacion.APROBADO);
        documento.setMotivoRechazo(null);
        documento.setFechaRevision(LocalDateTime.now());
        Documento guardado = documentoRepository.save(documento);
        actualizarEstadoCuenta(guardado);
        notificarDueno(guardado, describir(guardado) + ": " + guardado.getTipo().concordar("aprobad") + ".");
        return guardado;
    }

    /** "Licencia de conducción" o "SOAT del vehículo ABC123", para mensajes y auditoría. */
    public String describir(Documento documento) {
        String texto = documento.getTipo().getEtiqueta();
        if (documento.getVehiculo() != null) {
            texto += " del vehículo " + documento.getVehiculo().getPlaca();
        }
        return texto;
    }

    /** Nombre del dueño del documento */
    public String nombreDueno(Documento documento) {
        if (documento.getEmpresa() != null) {
            return documento.getEmpresa().getRazonSocial();
        }
        if (documento.getTransportador() != null) {
            return documento.getTransportador().getNombreCompleto();
        }
        return documento.getVehiculo().getTransportador().getNombreCompleto();
    }

    private void notificarDueno(Documento documento, String mensaje) {
        if (documento.getEmpresa() != null) {
            notificacionService.notificar(documento.getEmpresa().getUsuario(), mensaje, "/empresa/perfil#documentos");
        } else if (documento.getTransportador() != null) {
            notificacionService.notificar(documento.getTransportador().getUsuario(), mensaje, "/transportador/perfil#documentos");
        } else {
            notificacionService.notificar(documento.getVehiculo().getTransportador().getUsuario(), mensaje,
                    "/transportador/vehiculos/" + documento.getVehiculo().getId() + "/documentos");
        }
    }

    @Transactional
    public Documento rechazar(Long documentoId, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new NegocioException("Debes indicar el motivo del rechazo.");
        }
        if (motivo.trim().length() > LARGO_MAXIMO_MOTIVO) {
            throw new NegocioException("El motivo no puede tener más de " + LARGO_MAXIMO_MOTIVO + " caracteres.");
        }
        Documento documento = buscarPorId(documentoId);
        documento.setEstado(EstadoVerificacion.RECHAZADO);
        documento.setMotivoRechazo(motivo.trim());
        documento.setFechaRevision(LocalDateTime.now());
        Documento guardado = documentoRepository.save(documento);
        actualizarEstadoCuenta(guardado);
        notificarDueno(guardado, describir(guardado) + ": " + guardado.getTipo().concordar("rechazad")
                + ". Motivo: " + guardado.getMotivoRechazo() + " Sube uno nuevo.");
        return guardado;
    }

    /** Si cambia la categoría, la licencia subida queda rechazada para que suba la nueva */
    @Transactional
    public void pedirNuevaLicencia(Transportador transportador, String categoriaNueva) {
        documentoDe(transportador, TipoDocumento.LICENCIA_CONDUCCION).ifPresent(documento -> {
            documento.setEstado(EstadoVerificacion.RECHAZADO);
            documento.setMotivoRechazo("Cambiaste tu categoría a " + categoriaNueva
                    + ". Sube la licencia de conducción que la acredite.");
            documento.setFechaRevision(LocalDateTime.now());
            actualizarEstadoCuenta(documentoRepository.save(documento));
        });
    }

    /** Guarda en la cuenta el estado calculado de sus documentos */
    private void actualizarEstadoCuenta(Documento documento) {
        if (documento.getEmpresa() != null) {
            Empresa empresa = documento.getEmpresa();
            List<Documento> documentos = documentosDe(empresa);
            empresa.setEstadoVerificacion(estadoCalculado(DuenoDocumento.EMPRESA, documentos));
            empresa.setMotivoRechazo(motivoDeRechazo(documentos));
            empresaRepository.save(empresa);
        } else if (documento.getTransportador() != null) {
            Transportador transportador = documento.getTransportador();
            List<Documento> documentos = documentosDe(transportador);
            transportador.setEstadoVerificacion(estadoCalculado(DuenoDocumento.TRANSPORTADOR, documentos));
            transportador.setMotivoRechazo(motivoDeRechazo(documentos));
            transportadorRepository.save(transportador);
        }
    }

    // Resumen de los rechazos para mostrar a nivel de cuenta, ej. "RUT: ilegible"
    private String motivoDeRechazo(List<Documento> documentos) {
        String motivo = documentos.stream()
                .filter(d -> d.getEstado() == EstadoVerificacion.RECHAZADO)
                .map(d -> d.getTipo().getEtiqueta() + ": " + d.getMotivoRechazo())
                .reduce((a, b) -> a + " · " + b)
                .orElse(null);
        return motivo == null || motivo.length() <= LARGO_MAXIMO_MOTIVO ? motivo : motivo.substring(0, LARGO_MAXIMO_MOTIVO);
    }
}
