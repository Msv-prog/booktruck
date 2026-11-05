package com.booktruck.controller;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.Documento;
import com.booktruck.model.Empresa;
import com.booktruck.model.enums.DuenoDocumento;
import com.booktruck.model.enums.EstadoVerificacion;
import com.booktruck.model.enums.TipoDocumento;
import com.booktruck.util.Paginacion;
import com.booktruck.util.Textos;
import com.booktruck.service.CargaService;
import com.booktruck.service.EmpresaService;
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
@RequestMapping("/admin/empresas")
public class AdminEmpresaController {

    private final EmpresaService empresaService;
    private final CargaService cargaService;
    private final VerificacionService verificacionService;

    public AdminEmpresaController(EmpresaService empresaService, CargaService cargaService,
                                   VerificacionService verificacionService) {
        this.empresaService = empresaService;
        this.cargaService = cargaService;
        this.verificacionService = verificacionService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoVerificacion verificacion,
                          @RequestParam(required = false) String ciudad,
                          @RequestParam(required = false) String busqueda,
                          @RequestParam(required = false) Integer pagina,
                          Model model) {
        List<Empresa> todas = empresaService.listarTodas();
        Paginacion<Empresa> paginacion = Paginacion.de(empresaService.filtrar(todas, verificacion, ciudad, busqueda), pagina);
        List<Empresa> empresas = paginacion.getContenido();
        model.addAttribute("paginacion", paginacion);
        Map<Long, Long> cargasPorEmpresa = new HashMap<>();
        for (Empresa empresa : empresas) {
            cargasPorEmpresa.put(empresa.getId(), cargaService.contarPorEmpresa(empresa.getId()));
        }

        model.addAttribute("empresas", empresas);
        model.addAttribute("cargasPorEmpresa", cargasPorEmpresa);
        model.addAttribute("total", todas.size());
        model.addAttribute("ciudades", empresaService.ciudadesConEmpresas(todas));
        model.addAttribute("hayFiltros", verificacion != null || !Textos.vacio(ciudad) || !Textos.vacio(busqueda));
        model.addAttribute("verificacion", verificacion);
        model.addAttribute("ciudad", ciudad);
        model.addAttribute("busqueda", busqueda);
        return "admin/empresas";
    }

    // La aprobación o el rechazo se hace documento por documento (AdminDocumentoController)
    @GetMapping("/{id}/verificar")
    public String verificar(@PathVariable Long id, Model model) {
        Empresa empresa;
        try {
            empresa = empresaService.buscarPorId(id);
        } catch (NegocioException e) {
            return "redirect:/admin/empresas";
        }
        List<Documento> documentos = verificacionService.documentosDe(empresa);
        model.addAttribute("empresa", empresa);
        model.addAttribute("tiposDocumento", TipoDocumento.de(DuenoDocumento.EMPRESA));
        model.addAttribute("documentos", verificacionService.porTipo(documentos));
        model.addAttribute("estadoDocumentos", verificacionService.estadoCalculado(DuenoDocumento.EMPRESA, documentos));
        return "admin/verificar-empresa";
    }
}
