package com.booktruck.controller;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.SeguimientoUbicacion;
import com.booktruck.model.Transportador;
import com.booktruck.model.Viaje;
import com.booktruck.model.enums.EstadoViaje;
import com.booktruck.model.enums.MotivoCancelacion;
import com.booktruck.security.CustomUserDetails;
import com.booktruck.service.CalificacionService;
import com.booktruck.service.DocumentoService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import com.booktruck.service.SeguimientoService;
import com.booktruck.service.TransportadorService;
import com.booktruck.service.ViajeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/transportador/viajes")
public class ViajeTransportadorController {

    private final ViajeService viajeService;
    private final SeguimientoService seguimientoService;
    private final CalificacionService calificacionService;
    private final TransportadorService transportadorService;
    private final DocumentoService documentoService;

    public ViajeTransportadorController(ViajeService viajeService, SeguimientoService seguimientoService,
                                         CalificacionService calificacionService,
                                         TransportadorService transportadorService, DocumentoService documentoService) {
        this.viajeService = viajeService;
        this.seguimientoService = seguimientoService;
        this.calificacionService = calificacionService;
        this.transportadorService = transportadorService;
        this.documentoService = documentoService;
    }

    @ModelAttribute("transportador")
    public Transportador transportadorActual(@AuthenticationPrincipal CustomUserDetails principal) {
        return transportadorService.buscarPorUsuarioId(principal.getUsuarioId());
    }

    @GetMapping
    public String listar(@ModelAttribute(name = "transportador", binding = false) Transportador transportador, Model model) {
        List<Viaje> viajes = viajeService.listarPorTransportador(transportador);
        Set<Long> viajesCalificados = viajes.stream()
                .filter(calificacionService::yaCalificadoPorTransportador)
                .map(Viaje::getId)
                .collect(Collectors.toSet());

        model.addAttribute("viajes", viajes);
        model.addAttribute("viajesCalificados", viajesCalificados);
        return "transportador/viajes";
    }

    @PostMapping("/{viajeId}/iniciar")
    public String iniciar(@PathVariable Long viajeId, @ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                           RedirectAttributes redirectAttributes) {
        try {
            viajeService.iniciar(transportador, viajeId);
            redirectAttributes.addFlashAttribute("exito",
                    "Viaje iniciado. Recuerda registrar tu ubicación durante el trayecto.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transportador/viajes";
    }

    @GetMapping("/{viajeId}/entregar")
    public String formularioEntrega(@PathVariable Long viajeId,
                                    @ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                                    Model model, RedirectAttributes redirectAttributes) {
        try {
            Viaje viaje = viajeService.obtenerPropioTransportador(transportador, viajeId);
            if (viaje.getEstado() != EstadoViaje.EN_CURSO) {
                redirectAttributes.addFlashAttribute("error", "Solo puedes marcar como entregado un viaje que esté en curso.");
                return "redirect:/transportador/viajes";
            }
            model.addAttribute("viaje", viaje);
            return "transportador/entrega";
        } catch (NegocioException e) {
            return "redirect:/transportador/viajes?error";
        }
    }

    @PostMapping("/{viajeId}/entregar")
    public String entregar(@PathVariable Long viajeId, @ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                            @RequestParam(name = "prueba", required = false) MultipartFile prueba,
                            RedirectAttributes redirectAttributes) {
        try {
            viajeService.marcarEntregado(transportador, viajeId, prueba);
            redirectAttributes.addFlashAttribute("exito",
                    "Marcaste el viaje como entregado. Falta que la empresa revise la prueba y confirme la recepción.");
            return "redirect:/transportador/viajes";
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/transportador/viajes/" + viajeId + "/entregar";
        }
    }

    // Página con el detalle del viaje
    @GetMapping("/{viajeId}")
    public String detalle(@PathVariable Long viajeId,
                          @ModelAttribute(name = "transportador", binding = false) Transportador transportador, Model model) {
        try {
            Viaje viaje = viajeService.obtenerPropioTransportador(transportador, viajeId);
            model.addAttribute("viaje", viaje);
            model.addAttribute("puntos", seguimientoService.listarPorViaje(viaje));
            model.addAttribute("calificaciones", calificacionService.deViaje(viaje));
            model.addAttribute("calificado", calificacionService.yaCalificadoPorTransportador(viaje));
            return "transportador/viaje";
        } catch (NegocioException e) {
            return "redirect:/transportador/viajes?error";
        }
    }

    @GetMapping("/{viajeId}/prueba-entrega")
    public ResponseEntity<Resource> verPruebaEntrega(@PathVariable Long viajeId,
                                                     @ModelAttribute(name = "transportador", binding = false) Transportador transportador) {
        try {
            Viaje viaje = viajeService.obtenerPropioTransportador(transportador, viajeId);
            return documentoService.servir(viaje.getEvidenciaRuta(), viaje.getEvidenciaNombreOriginal());
        } catch (NegocioException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{viajeId}/cancelar")
    public String cancelar(@PathVariable Long viajeId, @ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                            @RequestParam(required = false) MotivoCancelacion motivo,
                            RedirectAttributes redirectAttributes) {
        try {
            viajeService.cancelarPorTransportador(transportador, viajeId, motivo);
            redirectAttributes.addFlashAttribute("exito", "Viaje cancelado correctamente.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transportador/viajes";
    }

    @GetMapping("/{viajeId}/ubicacion")
    public String ubicacion(@PathVariable Long viajeId, @ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                             Model model) {
        Viaje viaje;
        try {
            viaje = viajeService.obtenerPropioTransportador(transportador, viajeId);
        } catch (NegocioException e) {
            return "redirect:/transportador/viajes?error";
        }
        agregarEstadoUbicacion(viaje, model);
        return "transportador/ubicacion";
    }

    @PostMapping("/{viajeId}/ubicacion")
    public String registrarUbicacion(@PathVariable Long viajeId, @ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                                      @RequestParam(required = false) BigDecimal latitud,
                                      @RequestParam(required = false) BigDecimal longitud,
                                      @RequestParam(required = false) String observacion,
                                      RedirectAttributes redirectAttributes) {
        Viaje viaje;
        try {
            viaje = viajeService.obtenerPropioTransportador(transportador, viajeId);
        } catch (NegocioException e) {
            return "redirect:/transportador/viajes?error";
        }

        // Se redirige para evitar el error 500 y que la ubicación se reenvíe al recargar
        try {
            seguimientoService.registrar(viaje, latitud, longitud, observacion);
            redirectAttributes.addFlashAttribute("exito", "Ubicación registrada correctamente.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transportador/viajes/" + viajeId + "/ubicacion";
    }

    private void agregarEstadoUbicacion(Viaje viaje, Model model) {
        List<SeguimientoUbicacion> puntos = seguimientoService.listarPorViaje(viaje);
        boolean requiereActualizacion = viaje.getEstado() == EstadoViaje.EN_CURSO
                && seguimientoService.requiereActualizacion(puntos);

        model.addAttribute("viaje", viaje);
        model.addAttribute("puntos", puntos);
        model.addAttribute("requiereActualizacion", requiereActualizacion);
    }
}
