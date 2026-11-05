package com.booktruck.controller;

import com.booktruck.dto.CargaForm;
import com.booktruck.exception.NegocioException;
import com.booktruck.model.Carga;
import com.booktruck.model.Empresa;
import com.booktruck.model.enums.EstadoCarga;
import com.booktruck.model.enums.EstadoOferta;
import com.booktruck.repository.OfertaRepository;
import com.booktruck.security.CustomUserDetails;
import com.booktruck.service.CargaService;
import com.booktruck.service.EmpresaService;
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

@Controller
@RequestMapping("/empresa/cargas")
public class CargaController {

    private final CargaService cargaService;
    private final EmpresaService empresaService;
    private final OfertaRepository ofertaRepository;

    public CargaController(CargaService cargaService, EmpresaService empresaService, OfertaRepository ofertaRepository) {
        this.cargaService = cargaService;
        this.empresaService = empresaService;
        this.ofertaRepository = ofertaRepository;
    }

    @ModelAttribute("empresa")
    public Empresa empresaActual(@AuthenticationPrincipal CustomUserDetails principal) {
        return empresaService.buscarPorUsuarioId(principal.getUsuarioId());
    }

    @GetMapping
    public String listar(@ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                          @RequestParam(name = "estado", required = false) EstadoCarga estado,
                          Model model) {
        model.addAttribute("cargas", cargaService.listarPorEmpresa(empresa, estado));
        model.addAttribute("estadoSeleccionado", estado);
        return "empresa/mis-cargas";
    }

    @GetMapping("/nueva")
    public String formularioNuevaCarga(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CargaForm());
        }
        return "empresa/carga-form";
    }

    @PostMapping("/nueva")
    public String publicarCarga(@ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                                 @ModelAttribute("form") CargaForm form, Model model) {
        try {
            cargaService.publicar(empresa, form);
            return "redirect:/empresa/inicio?cargaPublicada";
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
            return "empresa/carga-form";
        }
    }

    @GetMapping("/{cargaId}/editar")
    public String formularioEditar(@ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                                   @PathVariable Long cargaId, Model model, RedirectAttributes redirectAttributes) {
        Carga carga;
        try {
            carga = cargaService.obtenerPropia(empresa, cargaId);
        } catch (NegocioException e) {
            return "redirect:/empresa/cargas?error";
        }
        if (!cargaService.esEditable(carga)) {
            redirectAttributes.addFlashAttribute("error", "Solo puedes editar cargas publicadas o vencidas.");
            return "redirect:/empresa/cargas";
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", CargaForm.desde(carga));
        }
        agregarDatosEdicion(carga, model);
        return "empresa/carga-form";
    }

    // Si hay un error se vuelve al formulario con los datos que se escribieron
    @PostMapping("/{cargaId}/editar")
    public String editar(@ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                         @PathVariable Long cargaId, @ModelAttribute("form") CargaForm form,
                         RedirectAttributes redirectAttributes) {
        try {
            int ofertasCerradas = cargaService.editar(empresa, cargaId, form);
            redirectAttributes.addFlashAttribute("exito", ofertasCerradas == 0
                    ? "Carga actualizada y publicada."
                    : "Carga actualizada y publicada. Como cambiaron datos importantes, "
                      + (ofertasCerradas == 1 ? "se cerró 1 oferta pendiente" : "se cerraron " + ofertasCerradas + " ofertas pendientes")
                      + " y se avisó a los transportadores para que vuelvan a ofertar.");
            return "redirect:/empresa/cargas";
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/empresa/cargas/" + cargaId + "/editar";
        }
    }

    private void agregarDatosEdicion(Carga carga, Model model) {
        model.addAttribute("cargaEditada", carga);
        model.addAttribute("ofertasPendientes", ofertaRepository.findByCargaIdAndEstado(carga.getId(), EstadoOferta.PENDIENTE).size());
    }

    @PostMapping("/{cargaId}/cancelar")
    public String cancelar(@ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                            @PathVariable Long cargaId,
                            RedirectAttributes redirectAttributes) {
        try {
            cargaService.cancelar(empresa, cargaId);
            redirectAttributes.addFlashAttribute("exito", "Carga cancelada correctamente.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empresa/cargas";
    }
}
