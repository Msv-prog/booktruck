package com.booktruck.controller;

import com.booktruck.dto.RegistroEmpresaForm;
import com.booktruck.dto.RegistroTransportadorForm;
import com.booktruck.exception.NegocioException;
import com.booktruck.service.EmpresaService;
import com.booktruck.service.TransportadorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final EmpresaService empresaService;
    private final TransportadorService transportadorService;

    public AuthController(EmpresaService empresaService, TransportadorService transportadorService) {
        this.empresaService = empresaService;
        this.transportadorService = transportadorService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/registro")
    public String registroSelector() {
        return "auth/registro-selector";
    }

    @GetMapping("/registro/empresa")
    public String formularioRegistroEmpresa(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegistroEmpresaForm());
        }
        return "auth/registro-empresa";
    }

    @PostMapping("/registro/empresa")
    public String procesarRegistroEmpresa(@ModelAttribute("form") RegistroEmpresaForm form, Model model) {
        try {
            empresaService.registrar(form);
            return "redirect:/auth/login?registrado";
        } catch (NegocioException e) {
            form.setPassword(null);
            form.setConfirmarPassword(null);
            model.addAttribute("error", e.getMessage());
            return "auth/registro-empresa";
        }
    }

    @GetMapping("/registro/transportador")
    public String formularioRegistroTransportador(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegistroTransportadorForm());
        }
        return "auth/registro-transportador";
    }

    @PostMapping("/registro/transportador")
    public String procesarRegistroTransportador(@ModelAttribute("form") RegistroTransportadorForm form, Model model) {
        try {
            transportadorService.registrar(form);
            return "redirect:/auth/login?registrado";
        } catch (NegocioException e) {
            form.setPassword(null);
            form.setConfirmarPassword(null);
            model.addAttribute("error", e.getMessage());
            return "auth/registro-transportador";
        }
    }
}