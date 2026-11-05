package com.booktruck.controller;

import com.booktruck.dto.CambiarPasswordForm;
import com.booktruck.dto.EmpresaPerfilForm;
import com.booktruck.dto.ResumenEmpresaDTO;
import com.booktruck.exception.NegocioException;
import com.booktruck.model.Empresa;
import com.booktruck.model.enums.DuenoDocumento;
import com.booktruck.model.enums.TipoDocumento;
import com.booktruck.security.CustomUserDetails;
import com.booktruck.service.CalificacionService;
import com.booktruck.service.DocumentoService;
import com.booktruck.service.EmpresaService;
import com.booktruck.service.OfertaService;
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
@RequestMapping("/empresa")
public class EmpresaController {

    private final EmpresaService empresaService;
    private final CalificacionService calificacionService;
    private final UsuarioService usuarioService;
    private final OfertaService ofertaService;
    private final DocumentoService documentoService;
    private final VerificacionService verificacionService;
    private final EstadisticasService estadisticasService;
    private final FotoPerfilService fotoPerfilService;

    public EmpresaController(EmpresaService empresaService, CalificacionService calificacionService,
                              UsuarioService usuarioService, OfertaService ofertaService,
                              DocumentoService documentoService, VerificacionService verificacionService,
                              EstadisticasService estadisticasService, FotoPerfilService fotoPerfilService) {
        this.empresaService = empresaService;
        this.calificacionService = calificacionService;
        this.usuarioService = usuarioService;
        this.ofertaService = ofertaService;
        this.documentoService = documentoService;
        this.verificacionService = verificacionService;
        this.estadisticasService = estadisticasService;
        this.fotoPerfilService = fotoPerfilService;
    }

    // Logo de la empresa
    @PostMapping("/perfil/foto")
    public String subirFoto(@ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                            @RequestParam(name = "foto", required = false) MultipartFile foto,
                            RedirectAttributes redirectAttributes) {
        try {
            fotoPerfilService.guardar(empresa.getUsuario(), foto);
            redirectAttributes.addFlashAttribute("exito", "Logo actualizado.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empresa/perfil";
    }

    @PostMapping("/perfil/foto/quitar")
    public String quitarFoto(@ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                             RedirectAttributes redirectAttributes) {
        fotoPerfilService.quitar(empresa.getUsuario());
        redirectAttributes.addFlashAttribute("exito", "Logo eliminado.");
        return "redirect:/empresa/perfil";
    }

    @ModelAttribute("empresa")
    public Empresa empresaActual(@AuthenticationPrincipal CustomUserDetails principal) {
        return empresaService.buscarPorUsuarioId(principal.getUsuarioId());
    }

    @GetMapping("/inicio")
    public String inicio(@ModelAttribute(name = "empresa", binding = false) Empresa empresa, Model model) {
        model.addAttribute("resumen", empresaService.obtenerResumen(empresa.getId()));
        model.addAttribute("graficas", estadisticasService.empresa(empresa));
        return "empresa/inicio";
    }

    // Datos que usa la página de perfil
    private void agregarDatosPerfil(Empresa empresa, Model model) {
        model.addAttribute("resumenCalificacion", calificacionService.resumenEmpresa(empresa.getId()));
        model.addAttribute("tiposDocumento", TipoDocumento.de(DuenoDocumento.EMPRESA));
        model.addAttribute("documentos", verificacionService.porTipo(verificacionService.documentosDe(empresa)));
        model.addAttribute("progreso", verificacionService.progreso(DuenoDocumento.EMPRESA, verificacionService.documentosDe(empresa)));
    }

    @PostMapping("/perfil/documentos/{tipo}")
    public String subirDocumentoVerificacion(@ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                                              @PathVariable TipoDocumento tipo,
                                              @RequestParam("archivo") MultipartFile archivo,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaVencimiento,
                                              RedirectAttributes redirectAttributes) {
        try {
            verificacionService.subir(empresa, tipo, archivo, fechaVencimiento);
            redirectAttributes.addFlashAttribute("exitoDocumento", tipo.getEtiqueta() + ": " + tipo.concordar("subid") + " correctamente. Queda pendiente de revisión.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("errorDocumento", tipo.getEtiqueta() + ": " + e.getMessage());
        }
        return "redirect:/empresa/perfil#documentos";
    }

    @GetMapping("/perfil/documentos/{tipo}/archivo")
    public ResponseEntity<Resource> verDocumentoVerificacion(@ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                                                             @PathVariable TipoDocumento tipo) {
        return documentoService.servir(verificacionService.documentoDe(empresa, tipo));
    }

    @GetMapping("/perfil")
    public String perfil(@ModelAttribute(name = "empresa", binding = false) Empresa empresa, Model model) {
        if (!model.containsAttribute("form")) {
            EmpresaPerfilForm form = new EmpresaPerfilForm();
            form.setRazonSocial(empresa.getRazonSocial());
            form.setTelefono(empresa.getTelefono());
            form.setCiudad(empresa.getCiudad());
            form.setDireccion(empresa.getDireccion());
            model.addAttribute("form", form);
        }
        agregarDatosPerfil(empresa, model);
        return "empresa/perfil";
    }

    @GetMapping("/ofertas")
    public String ofertasPendientes(@ModelAttribute(name = "empresa", binding = false) Empresa empresa, Model model) {
        model.addAttribute("ofertas", ofertaService.listarPendientesPorEmpresa(empresa));
        return "empresa/ofertas-pendientes";
    }

    @GetMapping("/calificaciones")
    public String calificaciones(@ModelAttribute(name = "empresa", binding = false) Empresa empresa, Model model) {
        model.addAttribute("calificaciones", calificacionService.listarRecibidasPorEmpresa(empresa));
        model.addAttribute("resumenCalificacion", calificacionService.resumenEmpresa(empresa.getId()));
        return "empresa/mis-calificaciones";
    }

    @PostMapping("/perfil")
    // Los dos formularios redirigen en vez de mostrar la página directamente (si no, daba error 500)
    public String actualizarPerfil(@ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                                    @ModelAttribute("form") EmpresaPerfilForm form,
                                    RedirectAttributes redirectAttributes) {
        try {
            empresaService.actualizarPerfil(empresa, form);
            redirectAttributes.addFlashAttribute("exito", "Perfil actualizado correctamente.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
        }
        return "redirect:/empresa/perfil";
    }

    @PostMapping("/perfil/password")
    public String cambiarPassword(@ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                                   @ModelAttribute("passwordForm") CambiarPasswordForm form,
                                   RedirectAttributes redirectAttributes) {
        try {
            usuarioService.cambiarPassword(empresa.getUsuario().getId(), form.getPasswordActual(),
                    form.getPasswordNueva(), form.getConfirmarPasswordNueva());
            redirectAttributes.addFlashAttribute("exitoPassword", "Contraseña actualizada correctamente.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("errorPassword", e.getMessage());
        }
        return "redirect:/empresa/perfil";
    }

    @PostMapping("/cuenta/desactivar")
    public String desactivarCuenta(@ModelAttribute(name = "empresa", binding = false) Empresa empresa,
                                    HttpServletRequest request, HttpServletResponse response) {
        usuarioService.desactivarCuentaPropia(empresa.getUsuario().getId());
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/auth/login?cuentaDesactivada";
    }
}
