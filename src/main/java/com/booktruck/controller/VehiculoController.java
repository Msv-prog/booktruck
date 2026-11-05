package com.booktruck.controller;

import com.booktruck.dto.VehiculoForm;
import com.booktruck.exception.NegocioException;
import com.booktruck.model.Vehiculo;
import com.booktruck.model.enums.DuenoDocumento;
import com.booktruck.model.enums.EstadoVerificacion;
import com.booktruck.model.enums.TipoDocumento;
import com.booktruck.security.CustomUserDetails;
import com.booktruck.model.Transportador;
import com.booktruck.service.DocumentoService;
import com.booktruck.service.TransportadorService;
import com.booktruck.service.VehiculoService;
import com.booktruck.service.VerificacionService;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/transportador/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;
    private final TransportadorService transportadorService;
    private final VerificacionService verificacionService;
    private final DocumentoService documentoService;

    public VehiculoController(VehiculoService vehiculoService, TransportadorService transportadorService,
                               VerificacionService verificacionService, DocumentoService documentoService) {
        this.vehiculoService = vehiculoService;
        this.transportadorService = transportadorService;
        this.verificacionService = verificacionService;
        this.documentoService = documentoService;
    }

    @ModelAttribute("transportador")
    public Transportador transportadorActual(@AuthenticationPrincipal CustomUserDetails principal) {
        return transportadorService.buscarPorUsuarioId(principal.getUsuarioId());
    }

    @GetMapping
    public String listar(@ModelAttribute(name = "transportador", binding = false) Transportador transportador, Model model) {
        List<Vehiculo> vehiculos = vehiculoService.listarPorTransportador(transportador);
        // Estado de los documentos de cada vehículo, para la columna "Documentos"
        Map<Long, EstadoVerificacion> estadoDocumentos = new HashMap<>();
        for (Vehiculo vehiculo : vehiculos) {
            estadoDocumentos.put(vehiculo.getId(),
                    verificacionService.estadoCalculado(DuenoDocumento.VEHICULO, verificacionService.documentosDe(vehiculo)));
        }
        model.addAttribute("vehiculos", vehiculos);
        model.addAttribute("estadoDocumentos", estadoDocumentos);
        return "transportador/vehiculos";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new VehiculoForm());
        }
        return "transportador/vehiculo-form";
    }

    @PostMapping("/nuevo")
    public String registrar(@ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                             @ModelAttribute("form") VehiculoForm form, Model model) {
        try {
            vehiculoService.registrar(transportador, form);
            return "redirect:/transportador/vehiculos?registrado";
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
            return "transportador/vehiculo-form";
        }
    }

    @PostMapping("/{vehiculoId}/estado")
    public String alternarEstado(@PathVariable Long vehiculoId,
                                  @ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                                  RedirectAttributes redirectAttributes) {
        try {
            vehiculoService.alternarEstado(transportador, vehiculoId);
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transportador/vehiculos";
    }

    @PostMapping("/{vehiculoId}/peso-bruto")
    public String completarPesoBruto(@PathVariable Long vehiculoId,
                                     @ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                                     @RequestParam(required = false) Integer pesoBrutoKg,
                                     RedirectAttributes redirectAttributes) {
        try {
            Vehiculo vehiculo = vehiculoService.completarPesoBruto(transportador, vehiculoId, pesoBrutoKg);
            redirectAttributes.addFlashAttribute("exito", "Peso bruto de " + vehiculo.getPlaca() + " guardado: requiere licencia "
                    + vehiculo.getLicenciaRequerida().name() + ".");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transportador/vehiculos";
    }

    @GetMapping("/{vehiculoId}/documentos")
    public String documentos(@PathVariable Long vehiculoId,
                              @ModelAttribute(name = "transportador", binding = false) Transportador transportador, Model model) {
        Vehiculo vehiculo;
        try {
            vehiculo = vehiculoService.obtenerPropio(transportador, vehiculoId);
        } catch (NegocioException e) {
            return "redirect:/transportador/vehiculos";
        }
        model.addAttribute("vehiculo", vehiculo);
        model.addAttribute("tiposDocumento", TipoDocumento.de(DuenoDocumento.VEHICULO));
        model.addAttribute("documentos", verificacionService.porTipo(verificacionService.documentosDe(vehiculo)));
        model.addAttribute("progreso", verificacionService.progreso(DuenoDocumento.VEHICULO, verificacionService.documentosDe(vehiculo)));
        return "transportador/vehiculo-documentos";
    }

    @PostMapping("/{vehiculoId}/documentos/{tipo}")
    public String subirDocumento(@PathVariable Long vehiculoId, @PathVariable TipoDocumento tipo,
                                  @ModelAttribute(name = "transportador", binding = false) Transportador transportador,
                                  @RequestParam("archivo") MultipartFile archivo,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaVencimiento,
                                  RedirectAttributes redirectAttributes) {
        Vehiculo vehiculo;
        try {
            vehiculo = vehiculoService.obtenerPropio(transportador, vehiculoId);
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/transportador/vehiculos";
        }
        try {
            verificacionService.subir(vehiculo, tipo, archivo, fechaVencimiento);
            redirectAttributes.addFlashAttribute("exitoDocumento", tipo.getEtiqueta() + ": " + tipo.concordar("subid") + " correctamente. Queda pendiente de revisión.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("errorDocumento", tipo.getEtiqueta() + ": " + e.getMessage());
        }
        return "redirect:/transportador/vehiculos/" + vehiculoId + "/documentos";
    }

    @GetMapping("/{vehiculoId}/documentos/{tipo}/archivo")
    public ResponseEntity<Resource> verDocumento(@PathVariable Long vehiculoId, @PathVariable TipoDocumento tipo,
                                                 @ModelAttribute(name = "transportador", binding = false) Transportador transportador) {
        try {
            Vehiculo vehiculo = vehiculoService.obtenerPropio(transportador, vehiculoId);
            return documentoService.servir(verificacionService.documentoDe(vehiculo, tipo));
        } catch (NegocioException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
