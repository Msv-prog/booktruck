package com.booktruck.controller;

import com.booktruck.dto.CrearAdminForm;
import com.booktruck.exception.NegocioException;
import com.booktruck.model.Usuario;
import com.booktruck.model.enums.RolUsuario;
import com.booktruck.security.CustomUserDetails;
import com.booktruck.service.AuditoriaService;
import com.booktruck.service.UsuarioService;
import com.booktruck.util.Paginacion;
import com.booktruck.util.Textos;
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

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;
    private final AuditoriaService auditoriaService;

    public AdminUsuarioController(UsuarioService usuarioService, AuditoriaService auditoriaService) {
        this.usuarioService = usuarioService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) RolUsuario rol,
                          @RequestParam(required = false) Boolean activo,
                          @RequestParam(required = false) String correo,
                          @RequestParam(required = false) Integer pagina,
                          Model model) {
        List<Usuario> todos = usuarioService.listarTodos();
        Paginacion<Usuario> paginacion = Paginacion.de(usuarioService.filtrar(todos, rol, activo, correo), pagina);
        model.addAttribute("usuarios", paginacion.getContenido());
        model.addAttribute("paginacion", paginacion);
        model.addAttribute("total", todos.size());
        model.addAttribute("hayFiltros", rol != null || activo != null || !Textos.vacio(correo));
        model.addAttribute("rol", rol);
        model.addAttribute("activo", activo);
        model.addAttribute("correo", correo);
        return "admin/usuarios";
    }

    @PostMapping("/{usuarioId}/estado")
    public String alternarEstado(@PathVariable Long usuarioId, @AuthenticationPrincipal CustomUserDetails principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.alternarActivo(usuarioId, principal.getUsuarioId());
            auditoriaService.registrar(usuario.getActivo() ? "ACTIVAR_USUARIO" : "DESACTIVAR_USUARIO",
                    usuario.getEmail() + " (" + usuario.getRol().name().toLowerCase() + ")");
            redirectAttributes.addFlashAttribute("exito", "La cuenta " + usuario.getEmail()
                    + (usuario.getActivo() ? " fue activada." : " fue desactivada."));
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/nuevo-admin")
    public String formularioCrearAdmin(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CrearAdminForm());
        }
        return "admin/crear-admin";
    }

    @PostMapping("/nuevo-admin")
    public String procesarCrearAdmin(@ModelAttribute("form") CrearAdminForm form, Model model) {
        try {
            Usuario nuevo = usuarioService.crearUsuarioBase(form.getEmail(), form.getPassword(), form.getConfirmarPassword(), RolUsuario.ADMINISTRADOR);
            auditoriaService.registrar("CREAR_ADMIN", nuevo.getEmail());
            return "redirect:/admin/usuarios?adminCreado";
        } catch (NegocioException e) {
            form.setPassword(null);
            form.setConfirmarPassword(null);
            model.addAttribute("error", e.getMessage());
            return "admin/crear-admin";
        }
    }
}
