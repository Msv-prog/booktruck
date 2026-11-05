package com.booktruck.controller;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.Usuario;
import com.booktruck.service.AuditoriaService;
import com.booktruck.service.FotoPerfilService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FotoPerfilController {

    private final FotoPerfilService fotoPerfilService;
    private final AuditoriaService auditoriaService;

    public FotoPerfilController(FotoPerfilService fotoPerfilService, AuditoriaService auditoriaService) {
        this.fotoPerfilService = fotoPerfilService;
        this.auditoriaService = auditoriaService;
    }

    // Cualquier usuario con sesión iniciada puede ver las fotos
    @GetMapping("/fotos/{usuarioId}")
    public ResponseEntity<Resource> ver(@PathVariable Long usuarioId) {
        return fotoPerfilService.servir(usuarioId);
    }

    @PostMapping("/admin/usuarios/{usuarioId}/foto/quitar")
    public String quitarComoAdmin(@PathVariable Long usuarioId, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = fotoPerfilService.quitarComoAdmin(usuarioId);
            auditoriaService.registrar("QUITAR_FOTO", usuario.getEmail());
            redirectAttributes.addFlashAttribute("exito", "Se quitó la foto de " + usuario.getEmail() + ".");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
