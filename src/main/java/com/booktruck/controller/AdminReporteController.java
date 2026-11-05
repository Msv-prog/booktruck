package com.booktruck.controller;

import com.booktruck.service.AdminReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reportes")
public class AdminReporteController {

    private final AdminReporteService adminReporteService;

    public AdminReporteController(AdminReporteService adminReporteService) {
        this.adminReporteService = adminReporteService;
    }

    @GetMapping
    public String reporte(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                           Model model) {
        model.addAttribute("reporte", adminReporteService.generar(desde, hasta));
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        return "admin/reportes";
    }
}
