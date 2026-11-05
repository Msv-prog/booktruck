package com.booktruck.controller;

import com.booktruck.model.Empresa;
import com.booktruck.model.Oferta;
import com.booktruck.model.Transportador;
import com.booktruck.model.enums.EstadoOferta;
import com.booktruck.service.OfertaService;
import com.booktruck.util.Paginacion;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin/ofertas")
public class AdminOfertaController {

    private final OfertaService ofertaService;

    public AdminOfertaController(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoOferta estado,
                          @RequestParam(required = false) Long empresaId,
                          @RequestParam(required = false) Long transportadorId,
                          @RequestParam(required = false) BigDecimal montoMinimo,
                          @RequestParam(required = false) BigDecimal montoMaximo,
                          @RequestParam(required = false) Integer pagina,
                          Model model) {
        List<Oferta> todas = ofertaService.listarTodas(null);
        Paginacion<Oferta> paginacion = Paginacion.de(
                ofertaService.filtrar(todas, estado, empresaId, transportadorId, montoMinimo, montoMaximo), pagina);
        model.addAttribute("ofertas", paginacion.getContenido());
        model.addAttribute("paginacion", paginacion);
        model.addAttribute("total", todas.size());

        // Opciones de las listas: solo empresas y transportadores que tienen ofertas
        Set<Long> empresasVistas = new HashSet<>();
        Set<Long> transportadoresVistos = new HashSet<>();
        model.addAttribute("empresas", todas.stream().map(o -> o.getCarga().getEmpresa())
                .filter(e -> empresasVistas.add(e.getId())).sorted(Comparator.comparing(Empresa::getRazonSocial)).toList());
        model.addAttribute("transportadores", todas.stream().map(Oferta::getTransportador)
                .filter(t -> transportadoresVistos.add(t.getId())).sorted(Comparator.comparing(Transportador::getNombreCompleto)).toList());

        model.addAttribute("hayFiltros", estado != null || empresaId != null || transportadorId != null
                || montoMinimo != null || montoMaximo != null);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("empresaId", empresaId);
        model.addAttribute("transportadorId", transportadorId);
        model.addAttribute("montoMinimo", montoMinimo);
        model.addAttribute("montoMaximo", montoMaximo);
        return "admin/ofertas";
    }
}
