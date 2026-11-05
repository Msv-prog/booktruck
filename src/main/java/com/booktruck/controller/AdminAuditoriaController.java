package com.booktruck.controller;

import com.booktruck.model.Auditoria;
import com.booktruck.model.Usuario;
import com.booktruck.service.AuditoriaService;
import com.booktruck.util.Paginacion;
import com.booktruck.util.Textos;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Registro de acciones de los administradores (quién aprobó, rechazó o desactivó qué y cuándo). */
@Controller
@RequestMapping("/admin/auditoria")
public class AdminAuditoriaController {

    private final AuditoriaService auditoriaService;

    public AdminAuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String accion,
                         @RequestParam(required = false) Long adminId,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         @RequestParam(required = false) String busqueda,
                         @RequestParam(required = false) Integer pagina,
                         Model model) {
        List<Auditoria> todos = auditoriaService.listarTodos();
        Paginacion<Auditoria> paginacion = Paginacion.de(
                auditoriaService.filtrar(todos, accion, adminId, desde, hasta, busqueda), pagina);
        model.addAttribute("registros", paginacion.getContenido());
        model.addAttribute("paginacion", paginacion);
        model.addAttribute("total", todos.size());

        // Opciones de los filtros
        Set<Long> vistos = new HashSet<>();
        model.addAttribute("acciones", AuditoriaService.ACCIONES);
        model.addAttribute("admins", todos.stream().map(Auditoria::getUsuario).filter(u -> vistos.add(u.getId()))
                .sorted(Comparator.comparing(Usuario::getEmail)).toList());

        model.addAttribute("hayFiltros", !Textos.vacio(accion) || adminId != null || desde != null || hasta != null
                || !Textos.vacio(busqueda));
        model.addAttribute("accion", accion);
        model.addAttribute("adminId", adminId);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("busqueda", busqueda);
        return "admin/auditoria";
    }
}
