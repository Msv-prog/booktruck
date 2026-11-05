package com.booktruck.controller;

import com.booktruck.service.AdminService;
import com.booktruck.service.EstadisticasService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final EstadisticasService estadisticasService;

    public AdminController(AdminService adminService, EstadisticasService estadisticasService) {
        this.adminService = adminService;
        this.estadisticasService = estadisticasService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("resumen", adminService.obtenerResumenGeneral());
        model.addAttribute("graficas", estadisticasService.admin());
        return "admin/dashboard";
    }
}
