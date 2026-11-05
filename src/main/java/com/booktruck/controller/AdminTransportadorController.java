package com.booktruck.controller;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.Documento;
import com.booktruck.model.Transportador;
import com.booktruck.model.Vehiculo;
import com.booktruck.model.enums.DuenoDocumento;
import com.booktruck.model.enums.EstadoVerificacion;
import com.booktruck.model.enums.TipoDocumento;
import com.booktruck.util.Paginacion;
import com.booktruck.util.Textos;
import com.booktruck.service.TransportadorService;
import com.booktruck.service.VehiculoService;
import com.booktruck.service.VerificacionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/transportadores")
public class AdminTransportadorController {

    private final TransportadorService transportadorService;
    private final VehiculoService vehiculoService;
    private final VerificacionService verificacionService;

    public AdminTransportadorController(TransportadorService transportadorService, VehiculoService vehiculoService,
                                         VerificacionService verificacionService) {
        this.transportadorService = transportadorService;
        this.vehiculoService = vehiculoService;
        this.verificacionService = verificacionService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoVerificacion verificacion,
                          @RequestParam(required = false) Boolean disponible,
                          @RequestParam(required = false) String licencia,
                          @RequestParam(required = false) String busqueda,
                          @RequestParam(required = false) Integer pagina,
                          Model model) {
        List<Transportador> todos = transportadorService.listarTodos();
        Paginacion<Transportador> paginacion = Paginacion.de(
                transportadorService.filtrar(todos, verificacion, disponible, licencia, busqueda), pagina);
        List<Transportador> transportadores = paginacion.getContenido();
        model.addAttribute("paginacion", paginacion);
        model.addAttribute("total", todos.size());
        model.addAttribute("hayFiltros", verificacion != null || disponible != null
                || !Textos.vacio(licencia) || !Textos.vacio(busqueda));
        model.addAttribute("verificacion", verificacion);
        model.addAttribute("disponible", disponible);
        model.addAttribute("licencia", licencia);
        model.addAttribute("busqueda", busqueda);
        Map<Long, Long> vehiculosPorTransportador = new HashMap<>();
        for (Transportador transportador : transportadores) {
            vehiculosPorTransportador.put(transportador.getId(), vehiculoService.contarPorTransportador(transportador.getId()));
        }

        model.addAttribute("transportadores", transportadores);
        model.addAttribute("vehiculosPorTransportador", vehiculosPorTransportador);
        return "admin/transportadores";
    }

    // La aprobación o el rechazo se hace documento por documento (AdminDocumentoController)
    @GetMapping("/{id}/verificar")
    public String verificar(@PathVariable Long id, Model model) {
        Transportador transportador;
        try {
            transportador = transportadorService.buscarPorId(id);
        } catch (NegocioException e) {
            return "redirect:/admin/transportadores";
        }
        List<Documento> documentos = verificacionService.documentosDe(transportador);
        List<Vehiculo> vehiculos = vehiculoService.listarPorTransportador(transportador);
        Map<Long, EstadoVerificacion> estadoVehiculos = new HashMap<>();
        for (Vehiculo vehiculo : vehiculos) {
            estadoVehiculos.put(vehiculo.getId(),
                    verificacionService.estadoCalculado(DuenoDocumento.VEHICULO, verificacionService.documentosDe(vehiculo)));
        }
        model.addAttribute("transportador", transportador);
        model.addAttribute("tiposDocumento", TipoDocumento.de(DuenoDocumento.TRANSPORTADOR));
        model.addAttribute("documentos", verificacionService.porTipo(documentos));
        model.addAttribute("estadoDocumentos", verificacionService.estadoCalculado(DuenoDocumento.TRANSPORTADOR, documentos));
        model.addAttribute("vehiculos", vehiculos);
        model.addAttribute("estadoVehiculos", estadoVehiculos);
        return "admin/verificar-transportador";
    }
}
