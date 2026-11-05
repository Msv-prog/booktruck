package com.booktruck.controller;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.Documento;
import com.booktruck.model.Vehiculo;
import com.booktruck.model.enums.DuenoDocumento;
import com.booktruck.model.enums.EstadoVerificacion;
import com.booktruck.model.enums.TipoCarroceria;
import com.booktruck.model.enums.TipoDocumento;
import com.booktruck.model.enums.TipoVehiculo;
import com.booktruck.service.AuditoriaService;
import com.booktruck.service.VehiculoService;
import com.booktruck.service.VerificacionService;
import com.booktruck.util.Paginacion;
import com.booktruck.util.Textos;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/vehiculos")
public class AdminVehiculoController {

    private final VehiculoService vehiculoService;
    private final VerificacionService verificacionService;
    private final AuditoriaService auditoriaService;

    public AdminVehiculoController(VehiculoService vehiculoService, VerificacionService verificacionService,
                                   AuditoriaService auditoriaService) {
        this.vehiculoService = vehiculoService;
        this.verificacionService = verificacionService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) TipoVehiculo tipo,
                          @RequestParam(required = false) TipoCarroceria carroceria,
                          @RequestParam(required = false) Boolean activo,
                          @RequestParam(required = false) String placa,
                          @RequestParam(required = false) Integer pagina,
                          Model model) {
        List<Vehiculo> todos = vehiculoService.listarTodos();
        Paginacion<Vehiculo> paginacion = Paginacion.de(vehiculoService.filtrar(todos, tipo, carroceria, activo, placa), pagina);
        List<Vehiculo> vehiculos = paginacion.getContenido();
        model.addAttribute("paginacion", paginacion);
        Map<Long, EstadoVerificacion> estadoDocumentos = new HashMap<>();
        for (Vehiculo vehiculo : vehiculos) {
            estadoDocumentos.put(vehiculo.getId(),
                    verificacionService.estadoCalculado(DuenoDocumento.VEHICULO, verificacionService.documentosDe(vehiculo)));
        }
        model.addAttribute("vehiculos", vehiculos);
        model.addAttribute("estadoDocumentos", estadoDocumentos);
        model.addAttribute("total", todos.size());
        model.addAttribute("carrocerias", TipoCarroceria.values());
        model.addAttribute("hayFiltros", tipo != null || carroceria != null || activo != null || !Textos.vacio(placa));
        model.addAttribute("tipo", tipo);
        model.addAttribute("carroceria", carroceria);
        model.addAttribute("activo", activo);
        model.addAttribute("placa", placa);
        return "admin/vehiculos";
    }

    // La aprobación o el rechazo se hace documento por documento (AdminDocumentoController)
    @GetMapping("/{vehiculoId}/verificar")
    public String verificar(@PathVariable Long vehiculoId, Model model) {
        Vehiculo vehiculo;
        try {
            vehiculo = vehiculoService.buscarPorId(vehiculoId);
        } catch (NegocioException e) {
            return "redirect:/admin/vehiculos";
        }
        List<Documento> documentos = verificacionService.documentosDe(vehiculo);
        model.addAttribute("vehiculo", vehiculo);
        model.addAttribute("tiposDocumento", TipoDocumento.de(DuenoDocumento.VEHICULO));
        model.addAttribute("documentos", verificacionService.porTipo(documentos));
        model.addAttribute("estadoDocumentos", verificacionService.estadoCalculado(DuenoDocumento.VEHICULO, documentos));
        return "admin/verificar-vehiculo";
    }

    @PostMapping("/{vehiculoId}/estado")
    public String alternarEstado(@PathVariable Long vehiculoId, RedirectAttributes redirectAttributes) {
        try {
            Vehiculo vehiculo = vehiculoService.alternarEstadoAdmin(vehiculoId);
            auditoriaService.registrar("CAMBIAR_ESTADO_VEHICULO", vehiculo.getPlaca() + (vehiculo.getActivo() ? " activado" : " desactivado")
                    + " (" + vehiculo.getTransportador().getNombreCompleto() + ")");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/vehiculos";
    }
}
