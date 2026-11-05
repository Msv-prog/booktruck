package com.booktruck.controller;

import com.booktruck.dto.CambiarPasswordForm;
import com.booktruck.dto.ResumenTransportadorDTO;
import com.booktruck.dto.TransportadorPerfilForm;
import com.booktruck.exception.NegocioException;
import com.booktruck.model.Transportador;
import com.booktruck.model.enums.DuenoDocumento;
import com.booktruck.model.enums.TipoDocumento;
import com.booktruck.security.CustomUserDetails;
import com.booktruck.service.CalificacionService;
import com.booktruck.service.DocumentoService;
import com.booktruck.service.OfertaService;
import com.booktruck.service.TransportadorService;
import com.booktruck.service.UsuarioService;
import com.booktruck.service.EstadisticasService;
import com.booktruck.service.FotoPerfilService;
import com.booktruck.service.VerificacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/transportador")
public class TransportadorController {

    private final TransportadorService transportadorService;
    private final CalificacionService calificacionService;
    private final UsuarioService usuarioService;
    private final OfertaService ofertaService;
    private final DocumentoService documentoService;
    private final VerificacionService verificacionService;
    private final EstadisticasService estadisticasService;
    private final FotoPerfilService fotoPerfilService;

    public TransportadorController(TransportadorService transportadorService, CalificacionService calificacionService,
                                    UsuarioService usuarioService, OfertaService ofertaService,
                                    DocumentoService documentoService, VerificacionService verificacionService,
                                    EstadisticasService estadisticasService, FotoPerfilService fotoPerfilService) {
        this.transportadorService = transportadorService;
        this.calificacionService = calificacionService;
        this.usuarioService = usuarioService;
        this.ofertaService = ofertaService;
        this.documentoService = documentoService;
        this.verificacionService = verificacionService;
        this.estadisticasService = estadisticasService;
        this.fotoPerfilService = fotoPerfilService;
    }

    // Foto de perfil
    @PostMapping("/perfil/foto")
    public String subirFoto(@ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                            @RequestParam(name = "foto", required = false) MultipartFile foto,
                            RedirectAttributes redirectAttributes) {
        try {
            fotoPerfilService.guardar(transportador.getUsuario(), foto);
            redirectAttributes.addFlashAttribute("exito", "Foto de perfil actualizada.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transportador/perfil";
    }

    @PostMapping("/perfil/foto/quitar")
    public String quitarFoto(@ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                             RedirectAttributes redirectAttributes) {
        fotoPerfilService.quitar(transportador.getUsuario());
        redirectAttributes.addFlashAttribute("exito", "Foto de perfil eliminada.");
        return "redirect:/transportador/perfil";
    }

    @ModelAttribute("transportador")
    public Transportador transportadorActual(@AuthenticationPrincipal CustomUserDetails principal) {
        return transportadorService.buscarPorUsuarioId(principal.getUsuarioId());
    }

    @GetMapping("/inicio")
    public String inicio(@ModelAttribute(name = "transportador", binding = false) Transportador transportador, Model model) {
        ResumenTransportadorDTO resumen = transportadorService.obtenerResumen(transportador.getId());
        model.addAttribute("resumen", resumen);
        model.addAttribute("graficas", estadisticasService.transportador(transportador));
        return "transportador/inicio";
    }

    @PostMapping("/disponibilidad")
    public String alternarDisponibilidad(@ModelAttribute(name = "transportador", binding = false) Transportador transportador) {
        transportadorService.alternarDisponibilidad(transportador);
        return "redirect:/transportador/inicio";
    }

    // Datos que usa la página de perfil
    private void agregarDatosPerfil(Transportador transportador, Model model) {
        model.addAttribute("resumenCalificacion", calificacionService.resumenTransportador(transportador.getId()));
        model.addAttribute("tiposDocumento", TipoDocumento.de(DuenoDocumento.TRANSPORTADOR));
        model.addAttribute("documentos", verificacionService.porTipo(verificacionService.documentosDe(transportador)));
        model.addAttribute("progreso", verificacionService.progreso(DuenoDocumento.TRANSPORTADOR, verificacionService.documentosDe(transportador)));
    }

    @PostMapping("/perfil/documentos/{tipo}")
    public String subirDocumentoVerificacion(@ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                                              @PathVariable TipoDocumento tipo,
                                              @RequestParam("archivo") MultipartFile archivo,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaVencimiento,
                                              RedirectAttributes redirectAttributes) {
        try {
            verificacionService.subir(transportador, tipo, archivo, fechaVencimiento);
            redirectAttributes.addFlashAttribute("exitoDocumento", tipo.getEtiqueta() + ": " + tipo.concordar("subid") + " correctamente. Queda pendiente de revisión.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("errorDocumento", tipo.getEtiqueta() + ": " + e.getMessage());
        }
        return "redirect:/transportador/perfil#documentos";
    }

    @GetMapping("/perfil/documentos/{tipo}/archivo")
    public ResponseEntity<Resource> verDocumentoVerificacion(@ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                                                             @PathVariable TipoDocumento tipo) {
        return documentoService.servir(verificacionService.documentoDe(transportador, tipo));
    }

    @GetMapping("/perfil")
    public String perfil(@ModelAttribute(name = "transportador", binding = false) Transportador transportador, Model model) {
        if (!model.containsAttribute("form")) {
            TransportadorPerfilForm form = new TransportadorPerfilForm();
            form.setNombreCompleto(transportador.getNombreCompleto());
            form.setTelefono(transportador.getTelefono());
            form.setLicencia(transportador.getLicencia());
            model.addAttribute("form", form);
        }
        agregarDatosPerfil(transportador, model);
        return "transportador/perfil";
    }

    @GetMapping("/ofertas")
    public String misOfertas(@ModelAttribute(name = "transportador", binding = false) Transportador transportador, Model model) {
        model.addAttribute("ofertas", ofertaService.listarPorTransportador(transportador));
        return "transportador/mis-ofertas";
    }

    @GetMapping("/calificaciones")
    public String calificaciones(@ModelAttribute(name = "transportador", binding = false) Transportador transportador, Model model) {
        model.addAttribute("calificaciones", calificacionService.listarRecibidasPorTransportador(transportador));
        model.addAttribute("resumenCalificacion", calificacionService.resumenTransportador(transportador.getId()));
        return "transportador/mis-calificaciones";
    }

    @PostMapping("/perfil")
    // Los dos formularios redirigen en vez de mostrar la página directamente (si no, daba error 500)
    public String actualizarPerfil(@ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                                    @ModelAttribute("form") TransportadorPerfilForm form,
                                    RedirectAttributes redirectAttributes) {
        try {
            boolean cambiaLicencia = transportadorService.cambiaLicencia(transportador, form);
            transportadorService.actualizarPerfil(transportador, form);
            redirectAttributes.addFlashAttribute("exito", cambiaLicencia
                    ? "Perfil actualizado. Como cambiaste la categoría de licencia, sube tu licencia de conducción "
                      + "de nuevo para que el administrador la verifique; mientras tanto no podrás ofertar."
                    : "Perfil actualizado correctamente.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
        }
        return "redirect:/transportador/perfil";
    }

    @PostMapping("/perfil/password")
    public String cambiarPassword(@ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                                   @ModelAttribute("passwordForm") CambiarPasswordForm form,
                                   RedirectAttributes redirectAttributes) {
        try {
            usuarioService.cambiarPassword(transportador.getUsuario().getId(), form.getPasswordActual(),
                    form.getPasswordNueva(), form.getConfirmarPasswordNueva());
            redirectAttributes.addFlashAttribute("exitoPassword", "Contraseña actualizada correctamente.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("errorPassword", e.getMessage());
        }
        return "redirect:/transportador/perfil";
    }

    @PostMapping("/cuenta/desactivar")
    public String desactivarCuenta(@ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                                    HttpServletRequest request, HttpServletResponse response) {
        usuarioService.desactivarCuentaPropia(transportador.getUsuario().getId());
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/auth/login?cuentaDesactivada";
    }
}
