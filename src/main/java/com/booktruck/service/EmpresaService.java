package com.booktruck.service;

import com.booktruck.dto.EmpresaPerfilForm;
import com.booktruck.dto.RegistroEmpresaForm;
import com.booktruck.dto.ResumenEmpresaDTO;
import com.booktruck.exception.NegocioException;
import com.booktruck.model.Empresa;
import com.booktruck.model.Usuario;
import com.booktruck.model.enums.EstadoCarga;
import com.booktruck.model.enums.EstadoOferta;
import com.booktruck.model.enums.EstadoVerificacion;
import com.booktruck.model.enums.EstadoViaje;
import com.booktruck.model.enums.RolUsuario;
import com.booktruck.util.Textos;
import com.booktruck.repository.CargaRepository;
import com.booktruck.repository.EmpresaRepository;
import com.booktruck.repository.OfertaRepository;
import com.booktruck.repository.ViajeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioService usuarioService;
    private final CargaRepository cargaRepository;
    private final OfertaRepository ofertaRepository;
    private final ViajeRepository viajeRepository;
    private final VerificacionService verificacionService;

    public EmpresaService(EmpresaRepository empresaRepository, UsuarioService usuarioService,
                           CargaRepository cargaRepository, OfertaRepository ofertaRepository,
                           ViajeRepository viajeRepository, VerificacionService verificacionService) {
        this.empresaRepository = empresaRepository;
        this.usuarioService = usuarioService;
        this.cargaRepository = cargaRepository;
        this.ofertaRepository = ofertaRepository;
        this.viajeRepository = viajeRepository;
        this.verificacionService = verificacionService;
    }

    @Transactional
    public Empresa registrar(RegistroEmpresaForm form) {
        if (form.getNit() == null || form.getNit().isBlank()) {
            throw new NegocioException("El NIT es obligatorio.");
        }
        String nitDigitos = form.getNit().replaceAll("\\D", "");
        if (nitDigitos.length() < 9 || nitDigitos.length() > 10) {
            throw new NegocioException("El NIT debe tener entre 9 y 10 dígitos (incluyendo el de verificación).");
        }
        validarDatosBasicos(form.getRazonSocial(), form.getTelefono(), form.getCiudad(), form.getDireccion());
        if (empresaRepository.existsByNit(form.getNit())) {
            throw new NegocioException("Ya existe una empresa registrada con ese NIT.");
        }

        Usuario usuario = usuarioService.crearUsuarioBase(
                form.getEmail(), form.getPassword(), form.getConfirmarPassword(), RolUsuario.EMPRESA);

        Empresa empresa = new Empresa();
        empresa.setUsuario(usuario);
        empresa.setNit(form.getNit());
        empresa.setRazonSocial(form.getRazonSocial());
        empresa.setTelefono(form.getTelefono());
        empresa.setCiudad(form.getCiudad());
        empresa.setDireccion(form.getDireccion());

        return empresaRepository.save(empresa);
    }

    public List<Empresa> listarTodas() {
        return empresaRepository.findAllByOrderByIdDesc();
    }

    // Filtros del admin (null o vacío = sin filtro)
    public List<Empresa> filtrar(List<Empresa> empresas, EstadoVerificacion verificacion, String ciudad, String busqueda) {
        return empresas.stream()
                .filter(e -> verificacion == null || verificacionService.estadoEmpresa(e) == verificacion)
                .filter(e -> Textos.vacio(ciudad) || ciudad.equals(e.getCiudad()))
                .filter(e -> Textos.coincide(busqueda, e.getRazonSocial(), e.getNit()))
                .toList();
    }

    // Ciudades que tienen al menos una empresa, para la lista del filtro
    public List<String> ciudadesConEmpresas(List<Empresa> empresas) {
        return empresas.stream().map(Empresa::getCiudad).filter(c -> !Textos.vacio(c))
                .distinct().sorted().toList();
    }

    public Empresa buscarPorUsuarioId(Long usuarioId) {
        return empresaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new NegocioException("No se encontró la empresa asociada al usuario."));
    }

    @Transactional
    public Empresa actualizarPerfil(Empresa empresa, EmpresaPerfilForm form) {
        validarDatosBasicos(form.getRazonSocial(), form.getTelefono(), form.getCiudad(), form.getDireccion());

        empresa.setRazonSocial(form.getRazonSocial());
        empresa.setTelefono(form.getTelefono());
        empresa.setCiudad(form.getCiudad());
        empresa.setDireccion(form.getDireccion());
        return empresaRepository.save(empresa);
    }

    private void validarDatosBasicos(String razonSocial, String telefono, String ciudad, String direccion) {
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new NegocioException("La razón social es obligatoria.");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new NegocioException("El teléfono es obligatorio.");
        }
        if (!telefono.matches("^[0-9]{7,10}$")) {
            throw new NegocioException("El teléfono debe tener entre 7 y 10 dígitos numéricos.");
        }
        if (ciudad == null || ciudad.isBlank()) {
            throw new NegocioException("La ciudad es obligatoria.");
        }
        if (direccion == null || direccion.isBlank()) {
            throw new NegocioException("La dirección es obligatoria.");
        }
    }

    public Empresa buscarPorId(Long empresaId) {
        return empresaRepository.findById(empresaId)
                .orElseThrow(() -> new NegocioException("La empresa no existe."));
    }

    public ResumenEmpresaDTO obtenerResumen(Long empresaId) {
        long cargasActivas = cargaRepository.countByEmpresaIdAndEstadoIn(
                empresaId, List.of(EstadoCarga.PUBLICADA, EstadoCarga.EN_PROCESO, EstadoCarga.ASIGNADA));
        long ofertasPendientes = ofertaRepository.countByCarga_EmpresaIdAndEstado(empresaId, EstadoOferta.PENDIENTE);
        long viajesEnCurso = viajeRepository.countByCarga_EmpresaIdAndEstado(empresaId, EstadoViaje.EN_CURSO);
        return new ResumenEmpresaDTO(
                cargasActivas,
                ofertasPendientes,
                viajesEnCurso,
                cargaRepository.findTop5ByEmpresaIdOrderByFechaPublicacionDesc(empresaId));
    }
}