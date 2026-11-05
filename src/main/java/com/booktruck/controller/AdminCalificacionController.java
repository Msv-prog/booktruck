package com.booktruck.controller;

import com.booktruck.model.Calificacion;
import com.booktruck.model.enums.TipoEvaluador;
import com.booktruck.service.CalificacionService;
import com.booktruck.util.Paginacion;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/calificaciones")
public class AdminCalificacionController {

    // Valor especial del filtro de puntuación: calificaciones de 2 estrellas o menos
    private static final String PROBLEMA = "problema";

    private final CalificacionService calificacionService;

    public AdminCalificacionController(CalificacionService calificacionService) {
        this.calificacionService = calificacionService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) TipoEvaluador evaluador,
                          @RequestParam(required = false) String puntuacion,
                          @RequestParam(required = false) Boolean problema,
                          @RequestParam(required = false) Integer pagina, Model model) {
        // "problema=true" se mantiene por compatibilidad con enlaces anteriores
        String filtroPuntuacion = Boolean.TRUE.equals(problema) ? PROBLEMA : puntuacion;
        Integer exacta = null;
        Integer maxima = null;
        if (PROBLEMA.equals(filtroPuntuacion)) {
            maxima = 2;
        } else if (filtroPuntuacion != null && filtroPuntuacion.matches("[1-5]")) {
            exacta = Integer.valueOf(filtroPuntuacion);
        } else {
            filtroPuntuacion = null;
        }

        List<Calificacion> todas = calificacionService.listarTodas(null);
        Paginacion<Calificacion> paginacion = Paginacion.de(calificacionService.filtrar(todas, evaluador, exacta, maxima), pagina);
        model.addAttribute("calificaciones", paginacion.getContenido());
        model.addAttribute("paginacion", paginacion);
        model.addAttribute("total", todas.size());
        model.addAttribute("hayFiltros", evaluador != null || filtroPuntuacion != null);
        model.addAttribute("evaluadorSeleccionado", evaluador);
        model.addAttribute("puntuacion", filtroPuntuacion);
        return "admin/calificaciones";
    }
}
