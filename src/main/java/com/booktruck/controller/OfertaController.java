package com.booktruck.controller;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.Carga;
import com.booktruck.model.Empresa;
import com.booktruck.security.CustomUserDetails;
import com.booktruck.service.CargaService;
import com.booktruck.service.EmpresaService;
import com.booktruck.service.OfertaService;
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
@RequestMapping("/empresa/cargas/{cargaId}/ofertas")
public class OfertaController {

    private final OfertaService ofertaService;
    private final CargaService cargaService;
    private final EmpresaService empresaService;

    public OfertaController(OfertaService ofertaService, CargaService cargaService, EmpresaService empresaService) {
        this.ofertaService = ofertaService;
        this.cargaService = cargaService;
        this.empresaService = empresaService;
    }

    @ModelAttribute("empresa")
    public Empresa empresaActual(@AuthenticationPrincipal CustomUserDetails principal) {
        return empresaService.buscarPorUsuarioId(principal.getUsuarioId());
    }

    @GetMapping
    public String listar(@PathVariable Long cargaId, @ModelAttribute(name = "empresa", binding = false) Empresa empresa, Model model) {
        try {
            Carga carga = cargaService.obtenerPropia(empresa, cargaId);
            model.addAttribute("carga", carga);
            model.addAttribute("ofertas", ofertaService.listarPorCarga(carga));
            return "empresa/ofertas-recibidas";
        } catch (NegocioException e) {
            return "redirect:/empresa/cargas?error";
        }
    }

    @PostMapping("/{ofertaId}/seleccionar")
    public String seleccionar(@PathVariable Long cargaId, @PathVariable Long ofertaId,
                               @ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                               RedirectAttributes redirectAttributes) {
        try {
            ofertaService.seleccionar(empresa, cargaId, ofertaId);
            redirectAttributes.addFlashAttribute("exito", "Oferta seleccionada. Se generó el viaje correspondiente.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empresa/cargas/" + cargaId + "/ofertas";
    }
}
