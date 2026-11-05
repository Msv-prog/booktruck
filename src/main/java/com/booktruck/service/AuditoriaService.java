package com.booktruck.service;

import com.booktruck.model.Auditoria;
import com.booktruck.repository.AuditoriaRepository;
import com.booktruck.repository.UsuarioRepository;
import com.booktruck.security.CustomUserDetails;
import com.booktruck.util.Textos;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registro de las acciones de los administradores; se consulta en /admin/auditoria. */
@Service
public class AuditoriaService {

    /** Acciones que se registran, con el texto que se muestra en el filtro y la tabla. */
    public static final Map<String, String> ACCIONES = new LinkedHashMap<>();
    static {
        ACCIONES.put("APROBAR_DOCUMENTO", "Aprobó un documento");
        ACCIONES.put("RECHAZAR_DOCUMENTO", "Rechazó un documento");
        ACCIONES.put("ACTIVAR_USUARIO", "Activó una cuenta");
        ACCIONES.put("DESACTIVAR_USUARIO", "Desactivó una cuenta");
        ACCIONES.put("CREAR_ADMIN", "Creó un administrador");
        ACCIONES.put("CAMBIAR_ESTADO_VEHICULO", "Activó o desactivó un vehículo");
        ACCIONES.put("CANCELAR_VIAJE", "Canceló un viaje");
        ACCIONES.put("QUITAR_FOTO", "Quitó una foto de perfil");
    }

    private static final int LARGO_MAXIMO = 255;

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository, UsuarioRepository usuarioRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Registra una acción del administrador que tiene la sesión abierta. */
    @Transactional
    public void registrar(String accion, String detalle) {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof CustomUserDetails principal)) {
            return;
        }
        Auditoria registro = new Auditoria();
        registro.setUsuario(usuarioRepository.getReferenceById(principal.getUsuarioId()));
        registro.setAccion(accion);
        registro.setDetalle(detalle.length() <= LARGO_MAXIMO ? detalle : detalle.substring(0, LARGO_MAXIMO - 1) + "…");
        auditoriaRepository.save(registro);
    }

    public List<Auditoria> listarTodos() {
        return auditoriaRepository.findAllByOrderByFechaDescIdDesc();
    }

    // Filtros (null o vacío = sin filtro)
    public List<Auditoria> filtrar(List<Auditoria> registros, String accion, Long adminId, LocalDate desde,
                                    LocalDate hasta, String busqueda) {
        return registros.stream()
                .filter(r -> Textos.vacio(accion) || accion.equals(r.getAccion()))
                .filter(r -> adminId == null || r.getUsuario().getId().equals(adminId))
                .filter(r -> desde == null || !r.getFecha().toLocalDate().isBefore(desde))
                .filter(r -> hasta == null || !r.getFecha().toLocalDate().isAfter(hasta))
                .filter(r -> Textos.coincide(busqueda, r.getDetalle()))
                .toList();
    }
}
