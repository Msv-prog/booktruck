package com.booktruck.controller;

import com.booktruck.dto.CalificacionForm;
import com.booktruck.exception.NegocioException;
import com.booktruck.model.Empresa;
import com.booktruck.model.Viaje;
import com.booktruck.model.enums.EstadoViaje;
import com.booktruck.security.CustomUserDetails;
import com.booktruck.service.CalificacionService;
import com.booktruck.service.EmpresaService;
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
@RequestMapping("/empresa/viajes/{viajeId}/calificar")
public class CalificacionController {

    private final CalificacionService calificacionService;
    private final ViajeService viajeService;
    private final EmpresaService empresaService;

    public CalificacionController(CalificacionService calificacionService, ViajeService viajeService,
                                   EmpresaService empresaService) {
        this.calificacionService = calificacionService;
        this.viajeService = viajeService;
        this.empresaService = empresaService;
    }

    @ModelAttribute("empresa")
    public Empresa empresaActual(@AuthenticationPrincipal CustomUserDetails principal) {
        return empresaService.buscarPorUsuarioId(principal.getUsuarioId());
    }

    @GetMapping
    public String formulario(@PathVariable Long viajeId, @ModelAttribute(name = "empresa", binding = false) Empresa empresa, Model model,
                              RedirectAttributes redirectAttributes) {
        Viaje viaje;
        try {
            viaje = viajeService.obtenerPropio(empresa, viajeId);
        } catch (NegocioException e) {
            return "redirect:/empresa/viajes?error";
        }
        if (calificacionService.yaCalificadoPorEmpresa(viaje)) {
            redirectAttributes.addFlashAttribute("error", "Ya calificaste este viaje.");
            return "redirect:/empresa/viajes";
        }

        model.addAttribute("viaje", viaje);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CalificacionForm());
        }
        return "empresa/calificar";
    }

    @PostMapping
    public String registrar(@PathVariable Long viajeId, @ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                             @ModelAttribute("form") CalificacionForm form,
                             RedirectAttributes redirectAttributes) {
        Viaje viaje;
        try {
            viaje = viajeService.obtenerPropio(empresa, viajeId);
        } catch (NegocioException e) {
            return "redirect:/empresa/viajes?error";
        }
        try {
            calificacionService.calificarComoEmpresa(viaje, form.getPuntuacion(), form.getComentario());
            redirectAttributes.addFlashAttribute("exito", "Calificación registrada correctamente.");
            return "redirect:/empresa/viajes";
        } catch (NegocioException e) {
            // Se redirige en vez de mostrar la página directamente (si no, daba error 500)
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            if (viaje.getEstado() != EstadoViaje.FINALIZADO || calificacionService.yaCalificadoPorEmpresa(viaje)) {
                return "redirect:/empresa/viajes";
            }
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/empresa/viajes/" + viajeId + "/calificar";
        }
    }
}
