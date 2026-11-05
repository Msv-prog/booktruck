package com.booktruck.controller;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.Empresa;
import com.booktruck.model.Transportador;
import com.booktruck.model.Viaje;
import com.booktruck.model.enums.EstadoViaje;
import com.booktruck.service.AuditoriaService;
import com.booktruck.service.CalificacionService;
import com.booktruck.service.DocumentoService;
import com.booktruck.service.SeguimientoService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import com.booktruck.service.ViajeService;
import com.booktruck.util.Paginacion;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin/viajes")
public class AdminViajeController {

    private final ViajeService viajeService;
    private final SeguimientoService seguimientoService;
    private final DocumentoService documentoService;
    private final AuditoriaService auditoriaService;
    private final CalificacionService calificacionService;

    public AdminViajeController(ViajeService viajeService, SeguimientoService seguimientoService,
                                DocumentoService documentoService, AuditoriaService auditoriaService,
                                CalificacionService calificacionService) {
        this.viajeService = viajeService;
        this.seguimientoService = seguimientoService;
        this.documentoService = documentoService;
        this.auditoriaService = auditoriaService;
        this.calificacionService = calificacionService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoViaje estado,
                          @RequestParam(required = false) Long empresaId,
                          @RequestParam(required = false) Long transportadorId,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                          @RequestParam(required = false) Integer pagina,
                          Model model) {
        List<Viaje> todos = viajeService.listarTodos(null);
        Paginacion<Viaje> paginacion = Paginacion.de(viajeService.filtrar(todos, estado, empresaId, transportadorId, desde, hasta), pagina);
        model.addAttribute("viajes", paginacion.getContenido());
        model.addAttribute("paginacion", paginacion);
        model.addAttribute("total", todos.size());

        // Opciones de las listas: solo empresas y transportadores que tienen viajes
        Set<Long> empresasVistas = new HashSet<>();
        Set<Long> transportadoresVistos = new HashSet<>();
        model.addAttribute("empresas", todos.stream().map(v -> v.getCarga().getEmpresa())
                .filter(e -> empresasVistas.add(e.getId())).sorted(Comparator.comparing(Empresa::getRazonSocial)).toList());
        model.addAttribute("transportadores", todos.stream().map(Viaje::getTransportador)
                .filter(t -> transportadoresVistos.add(t.getId())).sorted(Comparator.comparing(Transportador::getNombreCompleto)).toList());

        model.addAttribute("hayFiltros", estado != null || empresaId != null || transportadorId != null
                || desde != null || hasta != null);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("empresaId", empresaId);
        model.addAttribute("transportadorId", transportadorId);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        return "admin/viajes";
    }

    @GetMapping("/{viajeId}")
    public String detalle(@PathVariable Long viajeId, Model model) {
        try {
            Viaje viaje = viajeService.buscarPorId(viajeId);
            model.addAttribute("viaje", viaje);
            model.addAttribute("puntos", seguimientoService.listarPorViaje(viaje));
            model.addAttribute("calificaciones", calificacionService.deViaje(viaje));
            return "admin/viaje";
        } catch (NegocioException e) {
            return "redirect:/admin/viajes?error";
        }
    }

    @GetMapping("/{viajeId}/prueba-entrega")
    public ResponseEntity<Resource> verPruebaEntrega(@PathVariable Long viajeId) {
        try {
            Viaje viaje = viajeService.buscarPorId(viajeId);
            return documentoService.servir(viaje.getEvidenciaRuta(), viaje.getEvidenciaNombreOriginal());
        } catch (NegocioException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{viajeId}/cancelar")
    public String cancelar(@PathVariable Long viajeId, RedirectAttributes redirectAttributes) {
        try {
            Viaje viaje = viajeService.cancelarComoAdmin(viajeId);
            auditoriaService.registrar("CANCELAR_VIAJE", "Viaje #" + viaje.getId() + " " + viaje.getCarga().getOrigen()
                    + " → " + viaje.getCarga().getDestino() + " (" + viaje.getTransportador().getNombreCompleto() + ")");
            redirectAttributes.addFlashAttribute("exito", "Viaje cancelado correctamente.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/viajes";
    }

    @GetMapping("/{viajeId}/seguimiento")
    public String seguimiento(@PathVariable Long viajeId, Model model) {
        Viaje viaje;
        try {
            viaje = viajeService.buscarPorId(viajeId);
        } catch (NegocioException e) {
            return "redirect:/admin/viajes?error";
        }
        model.addAttribute("viaje", viaje);
        model.addAttribute("puntos", seguimientoService.listarPorViaje(viaje));
        return "admin/seguimiento";
    }
}
