package com.booktruck.controller;

import com.booktruck.exception.NegocioException;
import com.booktruck.security.CustomUserDetails;
import com.booktruck.service.EmpresaService;
import com.booktruck.service.NotificacionService;
import com.booktruck.service.TransportadorService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/** Página de notificaciones de empresas y transportadores (cada uno con su menú). */
@Controller
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final EmpresaService empresaService;
    private final TransportadorService transportadorService;

    public NotificacionController(NotificacionService notificacionService, EmpresaService empresaService,
                                  TransportadorService transportadorService) {
        this.notificacionService = notificacionService;
        this.empresaService = empresaService;
        this.transportadorService = transportadorService;
    }

    @GetMapping("/empresa/notificaciones")
    public String empresa(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("empresa", empresaService.buscarPorUsuarioId(principal.getUsuarioId()));
        return listar(principal, "/empresa", model);
    }

    @GetMapping("/transportador/notificaciones")
    public String transportador(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("transportador", transportadorService.buscarPorUsuarioId(principal.getUsuarioId()));
        return listar(principal, "/transportador", model);
    }

    private String listar(CustomUserDetails principal, String base, Model model) {
        model.addAttribute("notificaciones", notificacionService.listar(principal.getUsuarioId()));
        model.addAttribute("base", base);
        return "notificaciones";
    }

    // Marca la notificación como leída y lleva a la página relacionada
    @PostMapping({"/empresa/notificaciones/{id}/abrir", "/transportador/notificaciones/{id}/abrir"})
    public String abrir(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        String base = principal.getUsuario().getRol().name().equals("EMPRESA") ? "/empresa" : "/transportador";
        try {
            String enlace = notificacionService.abrir(principal.getUsuarioId(), id);
            // Solo se redirige a rutas internas de la app
            return "redirect:" + (enlace != null && enlace.startsWith("/") && !enlace.startsWith("//") ? enlace : base + "/notificaciones");
        } catch (NegocioException e) {
            return "redirect:" + base + "/notificaciones";
        }
    }

    @PostMapping({"/empresa/notificaciones/leidas", "/transportador/notificaciones/leidas"})
    public String marcarTodas(@AuthenticationPrincipal CustomUserDetails principal) {
        notificacionService.marcarTodasLeidas(principal.getUsuarioId());
        String base = principal.getUsuario().getRol().name().equals("EMPRESA") ? "/empresa" : "/transportador";
        return "redirect:" + base + "/notificaciones";
    }
}
