package com.booktruck.controller;

import com.booktruck.model.Carga;
import com.booktruck.model.Empresa;
import com.booktruck.model.enums.EstadoCarga;
import com.booktruck.model.enums.TipoCarroceria;
import com.booktruck.service.CargaService;
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
import java.util.function.Function;
import java.util.function.Predicate;

@Controller
@RequestMapping("/admin/cargas")
public class AdminCargaController {

    private final CargaService cargaService;

    public AdminCargaController(CargaService cargaService) {
        this.cargaService = cargaService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoCarga estado,
                          @RequestParam(required = false) String origen,
                          @RequestParam(required = false) String destino,
                          @RequestParam(required = false) String tipoMercancia,
                          @RequestParam(required = false) Long empresaId,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                          @RequestParam(required = false) TipoCarroceria carroceria,
                          @RequestParam(required = false) Integer pagina,
                          Model model) {
        List<Carga> todas = cargaService.listarTodas(null);
        Paginacion<Carga> paginacion = Paginacion.de(
                cargaService.filtrar(todas, estado, origen, destino, tipoMercancia, empresaId, desde, hasta, carroceria), pagina);
        model.addAttribute("cargas", paginacion.getContenido());
        model.addAttribute("paginacion", paginacion);
        model.addAttribute("carrocerias", TipoCarroceria.values());
        model.addAttribute("carroceria", carroceria);
        model.addAttribute("total", todas.size());

        // Opciones de las listas: solo valores que existen en las cargas
        model.addAttribute("origenes", valoresUnicos(todas, Carga::getOrigen));
        model.addAttribute("destinos", valoresUnicos(todas, Carga::getDestino));
        model.addAttribute("tiposMercancia", valoresUnicos(todas, Carga::getTipoMercancia));
        model.addAttribute("empresas", todas.stream().map(Carga::getEmpresa)
                .filter(distintoPorId()).sorted(Comparator.comparing(Empresa::getRazonSocial)).toList());

        model.addAttribute("hayFiltros", estado != null || !Textos.vacio(origen) || !Textos.vacio(destino)
                || !Textos.vacio(tipoMercancia) || empresaId != null || desde != null || hasta != null || carroceria != null);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("origen", origen);
        model.addAttribute("destino", destino);
        model.addAttribute("tipoMercancia", tipoMercancia);
        model.addAttribute("empresaId", empresaId);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        return "admin/cargas";
    }

    private static List<String> valoresUnicos(List<Carga> cargas, Function<Carga, String> campo) {
        return cargas.stream().map(campo).filter(v -> !Textos.vacio(v)).distinct().sorted().toList();
    }

    private static Predicate<Empresa> distintoPorId() {
        Set<Long> vistos = new HashSet<>();
        return empresa -> vistos.add(empresa.getId());
    }
}
