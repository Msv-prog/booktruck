package com.booktruck.controller;

import com.booktruck.dto.OfertaForm;
import com.booktruck.exception.NegocioException;
import com.booktruck.model.Carga;
import com.booktruck.model.Transportador;
import com.booktruck.model.Vehiculo;
import com.booktruck.security.CustomUserDetails;
import com.booktruck.service.CargaService;
import com.booktruck.service.OfertaService;
import com.booktruck.service.TransportadorService;
import com.booktruck.service.VehiculoService;
import com.booktruck.service.VerificacionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/transportador/cargas/{cargaId}/ofertar")
public class OfertaTransportadorController {

    private final OfertaService ofertaService;
    private final CargaService cargaService;
    private final VehiculoService vehiculoService;
    private final TransportadorService transportadorService;
    private final VerificacionService verificacionService;

    public OfertaTransportadorController(OfertaService ofertaService, CargaService cargaService,
                                          VehiculoService vehiculoService, TransportadorService transportadorService,
                                          VerificacionService verificacionService) {
        this.ofertaService = ofertaService;
        this.cargaService = cargaService;
        this.vehiculoService = vehiculoService;
        this.transportadorService = transportadorService;
        this.verificacionService = verificacionService;
    }

    @ModelAttribute("transportador")
    public Transportador transportadorActual(@AuthenticationPrincipal CustomUserDetails principal) {
        return transportadorService.buscarPorUsuarioId(principal.getUsuarioId());
    }

    private List<Vehiculo> vehiculosActivosDe(Transportador transportador) {
        return vehiculoService.listarPorTransportador(transportador).stream()
                .filter(Vehiculo::getActivo)
                .toList();
    }

    // Solo se muestran los vehículos con capacidad, permitidos por la licencia y con documentos al día
    private void cargarVehiculos(Model model, Transportador transportador, Carga carga) {
        List<Vehiculo> activos = vehiculosActivosDe(transportador);
        List<Vehiculo> conCapacidad = activos.stream()
                .filter(vehiculo -> ofertaService.tieneCapacidadPara(vehiculo, carga))
                .toList();
        // Motivo por el que no sirve cada vehículo (carrocería o licencia)
        List<String> motivos = new java.util.ArrayList<>();
        List<Vehiculo> permitidos = new java.util.ArrayList<>();
        for (Vehiculo vehiculo : conCapacidad) {
            String motivo = ofertaService.problemaDeCarroceria(vehiculo, carga);
            if (motivo == null) {
                motivo = ofertaService.problemaDeLicencia(transportador, vehiculo);
            }
            if (motivo == null) {
                permitidos.add(vehiculo);
            } else {
                motivos.add(motivo);
            }
        }
        model.addAttribute("verificado", verificacionService.transportadorVerificado(transportador));
        model.addAttribute("tieneVehiculosActivos", !activos.isEmpty());
        model.addAttribute("vehiculosActivos", permitidos.stream().filter(verificacionService::vehiculoHabilitado).toList());
        model.addAttribute("motivosExcluidos", motivos);
        model.addAttribute("placasSinDocumentos", permitidos.stream()
                .filter(vehiculo -> !verificacionService.vehiculoHabilitado(vehiculo))
                .map(Vehiculo::getPlaca).toList());
    }

    @GetMapping
    public String formulario(@PathVariable Long cargaId, @ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                              Model model) {
        Carga carga;
        try {
            carga = cargaService.buscarPorId(cargaId);
        } catch (NegocioException e) {
            return "redirect:/transportador/cargas?error";
        }

        model.addAttribute("carga", carga);
        cargarVehiculos(model, transportador, carga);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new OfertaForm());
        }
        return "transportador/oferta-form";
    }

    @PostMapping
    public String registrar(@PathVariable Long cargaId, @ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                             @ModelAttribute("form") OfertaForm form, Model model) {
        Carga carga;
        try {
            carga = cargaService.buscarPorId(cargaId);
        } catch (NegocioException e) {
            return "redirect:/transportador/cargas?error";
        }

        try {
            ofertaService.crear(transportador, carga, form);
            return "redirect:/transportador/cargas?ofertaEnviada";
        } catch (NegocioException e) {
            model.addAttribute("carga", carga);
            cargarVehiculos(model, transportador, carga);
            model.addAttribute("error", e.getMessage());
            return "transportador/oferta-form";
        }
    }
}
