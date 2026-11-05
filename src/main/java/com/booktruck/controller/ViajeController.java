package com.booktruck.controller;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.Empresa;
import com.booktruck.model.Viaje;
import com.booktruck.model.enums.MotivoCancelacion;
import com.booktruck.security.CustomUserDetails;
import com.booktruck.service.CalificacionService;
import com.booktruck.service.DocumentoService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import com.booktruck.service.EmpresaService;
import com.booktruck.service.SeguimientoService;
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/empresa/viajes")
public class ViajeController {

    private final ViajeService viajeService;
    private final SeguimientoService seguimientoService;
    private final CalificacionService calificacionService;
    private final EmpresaService empresaService;
    private final DocumentoService documentoService;

    public ViajeController(ViajeService viajeService, SeguimientoService seguimientoService,
                            CalificacionService calificacionService, EmpresaService empresaService,
                            DocumentoService documentoService) {
        this.viajeService = viajeService;
        this.seguimientoService = seguimientoService;
        this.calificacionService = calificacionService;
        this.empresaService = empresaService;
        this.documentoService = documentoService;
    }

    @ModelAttribute("empresa")
    public Empresa empresaActual(@AuthenticationPrincipal CustomUserDetails principal) {
        return empresaService.buscarPorUsuarioId(principal.getUsuarioId());
    }

    @GetMapping
    public String listar(@ModelAttribute(name = "empresa", binding = false) Empresa empresa, Model model) {
        List<Viaje> viajes = viajeService.listarPorEmpresa(empresa);
        Set<Long> viajesCalificados = viajes.stream()
                .filter(calificacionService::yaCalificadoPorEmpresa)
                .map(Viaje::getId)
                .collect(Collectors.toSet());

        model.addAttribute("viajes", viajes);
        model.addAttribute("viajesCalificados", viajesCalificados);
        return "empresa/viajes";
    }

    @PostMapping("/{viajeId}/confirmar")
    public String confirmar(@PathVariable Long viajeId, @ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                             RedirectAttributes redirectAttributes) {
        try {
            viajeService.confirmarRecepcion(empresa, viajeId);
            redirectAttributes.addFlashAttribute("exito", "Recepción confirmada. El viaje quedó finalizado.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empresa/viajes";
    }

    @PostMapping("/{viajeId}/cancelar")
    public String cancelar(@PathVariable Long viajeId, @ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                            @RequestParam(required = false) MotivoCancelacion motivo,
                            RedirectAttributes redirectAttributes) {
        try {
            viajeService.cancelarPorEmpresa(empresa, viajeId, motivo);
            redirectAttributes.addFlashAttribute("exito", "Viaje cancelado correctamente.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empresa/viajes";
    }

    // Página con el detalle del viaje
    @GetMapping("/{viajeId}")
    public String detalle(@PathVariable Long viajeId, @ModelAttribute(name = "empresa", binding = false) Empresa empresa, Model model) {
        try {
            Viaje viaje = viajeService.obtenerPropio(empresa, viajeId);
            model.addAttribute("viaje", viaje);
            model.addAttribute("puntos", seguimientoService.listarPorViaje(viaje));
            model.addAttribute("calificaciones", calificacionService.deViaje(viaje));
            model.addAttribute("calificado", calificacionService.yaCalificadoPorEmpresa(viaje));
            return "empresa/viaje";
        } catch (NegocioException e) {
            return "redirect:/empresa/viajes?error";
        }
    }

    @GetMapping("/{viajeId}/prueba-entrega")
    public ResponseEntity<Resource> verPruebaEntrega(@PathVariable Long viajeId,
                                                     @ModelAttribute(name = "empresa", binding = false) Empresa empresa) {
        try {
            Viaje viaje = viajeService.obtenerPropio(empresa, viajeId);
            return documentoService.servir(viaje.getEvidenciaRuta(), viaje.getEvidenciaNombreOriginal());
        } catch (NegocioException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{viajeId}/seguimiento")
    public String seguimiento(@PathVariable Long viajeId, @ModelAttribute(name = "empresa", binding = false) Empresa empresa, Model model) {
        try {
            Viaje viaje = viajeService.obtenerPropio(empresa, viajeId);
            model.addAttribute("viaje", viaje);
            model.addAttribute("puntos", seguimientoService.listarPorViaje(viaje));
            return "empresa/seguimiento";
        } catch (NegocioException e) {
            return "redirect:/empresa/viajes?error";
        }
    }
}
