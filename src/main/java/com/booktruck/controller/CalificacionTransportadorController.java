package com.booktruck.controller;

import com.booktruck.dto.CalificacionForm;
import com.booktruck.exception.NegocioException;
import com.booktruck.model.Transportador;
import com.booktruck.model.Viaje;
import com.booktruck.model.enums.EstadoViaje;
import com.booktruck.security.CustomUserDetails;
import com.booktruck.service.CalificacionService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/transportador/viajes/{viajeId}/calificar")
public class CalificacionTransportadorController {

    private final CalificacionService calificacionService;
    private final ViajeService viajeService;
    private final TransportadorService transportadorService;

    public CalificacionTransportadorController(CalificacionService calificacionService, ViajeService viajeService,
                                                TransportadorService transportadorService) {
        this.calificacionService = calificacionService;
        this.viajeService = viajeService;
        this.transportadorService = transportadorService;
    }

    @ModelAttribute("transportador")
    public Transportador transportadorActual(@AuthenticationPrincipal CustomUserDetails principal) {
        return transportadorService.buscarPorUsuarioId(principal.getUsuarioId());
    }

    @GetMapping
    public String formulario(@PathVariable Long viajeId, @ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                              Model model, RedirectAttributes redirectAttributes) {
        Viaje viaje;
        try {
            viaje = viajeService.obtenerPropioTransportador(transportador, viajeId);
        } catch (NegocioException e) {
            return "redirect:/transportador/viajes?error";
        }
        if (calificacionService.yaCalificadoPorTransportador(viaje)) {
            redirectAttributes.addFlashAttribute("error", "Ya calificaste este viaje.");
            return "redirect:/transportador/viajes";
        }

        model.addAttribute("viaje", viaje);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CalificacionForm());
        }
        return "transportador/calificar";
    }

    @PostMapping
    public String registrar(@PathVariable Long viajeId, @ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                             @ModelAttribute("form") CalificacionForm form,
                             RedirectAttributes redirectAttributes) {
        Viaje viaje;
        try {
            viaje = viajeService.obtenerPropioTransportador(transportador, viajeId);
        } catch (NegocioException e) {
            return "redirect:/transportador/viajes?error";
        }
        try {
            calificacionService.calificarComoTransportador(viaje, form.getPuntuacion(), form.getComentario());
            redirectAttributes.addFlashAttribute("exito", "Calificación registrada correctamente.");
            return "redirect:/transportador/viajes";
        } catch (NegocioException e) {
            // Se redirige en vez de mostrar la página directamente (si no, daba error 500)
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            if (viaje.getEstado() != EstadoViaje.FINALIZADO || calificacionService.yaCalificadoPorTransportador(viaje)) {
                return "redirect:/transportador/viajes";
            }
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/transportador/viajes/" + viajeId + "/calificar";
        }
    }
}
