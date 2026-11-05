package com.booktruck.controller;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.Carga;
import com.booktruck.model.Oferta;
import com.booktruck.model.Transportador;
import com.booktruck.model.Vehiculo;
import com.booktruck.security.CustomUserDetails;
import com.booktruck.service.CargaService;
import com.booktruck.service.OfertaService;
import com.booktruck.service.TransportadorService;
import com.booktruck.service.VerificacionService;
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

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/transportador/cargas")
public class CargaDisponibleController {

    private final CargaService cargaService;
    private final OfertaService ofertaService;
    private final TransportadorService transportadorService;
    private final VerificacionService verificacionService;

    public CargaDisponibleController(CargaService cargaService, OfertaService ofertaService,
                                      TransportadorService transportadorService, VerificacionService verificacionService) {
        this.cargaService = cargaService;
        this.ofertaService = ofertaService;
        this.transportadorService = transportadorService;
        this.verificacionService = verificacionService;
    }

    @ModelAttribute("transportador")
    public Transportador transportadorActual(@AuthenticationPrincipal CustomUserDetails principal) {
        return transportadorService.buscarPorUsuarioId(principal.getUsuarioId());
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String origen,
                          @RequestParam(required = false) String destino,
                          @RequestParam(required = false) String tipoMercancia,
                          @RequestParam(required = false) BigDecimal pesoMaximo,
                          @RequestParam(required = false) BigDecimal precioMinimo,
                          @RequestParam(required = false) BigDecimal precioMaximo,
                          @RequestParam(required = false) Boolean puedoTomar,
                          @RequestParam(required = false) String orden,
                          @ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                          Model model) {
        List<Carga> cargas = cargaService.listarPublicadasDisponibles(
                origen, destino, tipoMercancia, pesoMaximo, precioMinimo, precioMaximo);

        // Cargas en las que alguno de sus vehículos listos cabe y tiene la carrocería pedida
        List<Vehiculo> listos = ofertaService.vehiculosListos(transportador);
        Set<Long> cargasQuePuedeTomar = cargas.stream()
                .filter(c -> ofertaService.puedeTomar(listos, c))
                .map(Carga::getId)
                .collect(Collectors.toSet());
        if (Boolean.TRUE.equals(puedoTomar)) {
            cargas = cargas.stream().filter(c -> cargasQuePuedeTomar.contains(c.getId())).toList();
        }
        cargas = ordenar(cargas, orden);
        Map<Long, Oferta> ofertaPorCarga = ofertaService.listarPorTransportador(transportador).stream()
                .collect(Collectors.toMap(oferta -> oferta.getCarga().getId(), Function.identity()));

        Set<Long> cargasReofertables = ofertaPorCarga.values().stream()
                .filter(ofertaService::puedeVolverAOfertar)
                .map(oferta -> oferta.getCarga().getId())
                .collect(Collectors.toSet());

        model.addAttribute("cargas", cargas);
        model.addAttribute("ofertaPorCarga", ofertaPorCarga);
        model.addAttribute("cargasReofertables", cargasReofertables);
        model.addAttribute("verificado", verificacionService.transportadorVerificado(transportador));
        model.addAttribute("origen", origen);
        model.addAttribute("destino", destino);
        model.addAttribute("tipoMercancia", tipoMercancia);
        model.addAttribute("pesoMaximo", pesoMaximo);
        model.addAttribute("precioMinimo", precioMinimo);
        model.addAttribute("precioMaximo", precioMaximo);
        model.addAttribute("puedoTomar", Boolean.TRUE.equals(puedoTomar));
        model.addAttribute("orden", orden == null ? "recientes" : orden);
        model.addAttribute("cargasQuePuedeTomar", cargasQuePuedeTomar);
        return "transportador/cargas-disponibles";
    }

    private static List<Carga> ordenar(List<Carga> cargas, String orden) {
        Comparator<Carga> comparador = switch (orden == null ? "" : orden) {
            case "precio_mayor" -> Comparator.comparing(Carga::getPrecioOfrecido).reversed();
            case "precio_menor" -> Comparator.comparing(Carga::getPrecioOfrecido);
            case "peso_menor" -> Comparator.comparing(Carga::getPeso);
            case "recoleccion" -> Comparator.comparing(Carga::getFechaRecoleccion);
            default -> Comparator.comparing(Carga::getFechaPublicacion).reversed();
        };
        return cargas.stream().sorted(comparador).toList();
    }

    @PostMapping("/{cargaId}/ofertar/retirar")
    public String retirarOferta(@PathVariable Long cargaId, @ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                                 RedirectAttributes redirectAttributes) {
        try {
            ofertaService.retirarPorCarga(transportador, cargaId);
            redirectAttributes.addFlashAttribute("exito", "Oferta retirada correctamente.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transportador/cargas";
    }
}
