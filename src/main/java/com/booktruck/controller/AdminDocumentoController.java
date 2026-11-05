package com.booktruck.controller;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.Documento;
import com.booktruck.service.AuditoriaService;
import com.booktruck.service.DocumentoService;
import com.booktruck.service.VerificacionService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/** Revisión de documentos de verificación (de empresas, transportadores y vehículos) por el administrador. */
@Controller
@RequestMapping("/admin/documentos")
public class AdminDocumentoController {

    private final VerificacionService verificacionService;
    private final DocumentoService documentoService;
    private final AuditoriaService auditoriaService;

    public AdminDocumentoController(VerificacionService verificacionService, DocumentoService documentoService,
                                    AuditoriaService auditoriaService) {
        this.verificacionService = verificacionService;
        this.documentoService = documentoService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/{documentoId}/archivo")
    public ResponseEntity<Resource> verArchivo(@PathVariable Long documentoId) {
        try {
            return documentoService.servir(Optional.of(verificacionService.buscarPorId(documentoId)));
        } catch (NegocioException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{documentoId}/aprobar")
    public String aprobar(@PathVariable Long documentoId, RedirectAttributes redirectAttributes) {
        try {
            Documento documento = verificacionService.aprobar(documentoId);
            auditoriaService.registrar("APROBAR_DOCUMENTO",
                    verificacionService.describir(documento) + " de " + verificacionService.nombreDueno(documento));
            redirectAttributes.addFlashAttribute("exito", documento.getTipo().getEtiqueta() + ": " + documento.getTipo().concordar("aprobad") + ".");
            return "redirect:" + paginaDelDueno(documento);
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/{documentoId}/rechazar")
    public String rechazar(@PathVariable Long documentoId, @RequestParam(required = false) String motivo,
                           RedirectAttributes redirectAttributes) {
        Documento documento;
        try {
            documento = verificacionService.buscarPorId(documentoId);
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/dashboard";
        }
        try {
            verificacionService.rechazar(documentoId, motivo);
            auditoriaService.registrar("RECHAZAR_DOCUMENTO", verificacionService.describir(documento) + " de "
                    + verificacionService.nombreDueno(documento) + ". Motivo: " + motivo.trim());
            redirectAttributes.addFlashAttribute("exito", documento.getTipo().getEtiqueta() + ": " + documento.getTipo().concordar("rechazad") + ".");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", documento.getTipo().getEtiqueta() + ": " + e.getMessage());
        }
        return "redirect:" + paginaDelDueno(documento);
    }

    private String paginaDelDueno(Documento documento) {
        if (documento.getEmpresa() != null) {
            return "/admin/empresas/" + documento.getEmpresa().getId() + "/verificar";
        }
        if (documento.getTransportador() != null) {
            return "/admin/transportadores/" + documento.getTransportador().getId() + "/verificar";
        }
        return "/admin/vehiculos/" + documento.getVehiculo().getId() + "/verificar";
    }
}
