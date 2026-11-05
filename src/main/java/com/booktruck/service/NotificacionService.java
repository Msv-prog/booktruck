package com.booktruck.service;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.Notificacion;
import com.booktruck.model.Usuario;
import com.booktruck.repository.NotificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Notificaciones dentro de la app (campanita) */
@Service
public class NotificacionService {

    private static final int LARGO_MAXIMO = 255;

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @Transactional
    public void notificar(Usuario usuario, String mensaje, String enlace) {
        if (usuario == null || mensaje == null || mensaje.isBlank()) {
            return;
        }
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setMensaje(mensaje.length() <= LARGO_MAXIMO ? mensaje : mensaje.substring(0, LARGO_MAXIMO - 1) + "…");
        notificacion.setEnlace(enlace);
        notificacion.setLeida(false);
        notificacionRepository.save(notificacion);
    }

    public List<Notificacion> listar(Long usuarioId) {
        return notificacionRepository.findTop50ByUsuarioIdOrderByFechaDescIdDesc(usuarioId);
    }

    public List<Notificacion> recientes(Long usuarioId) {
        return notificacionRepository.findTop5ByUsuarioIdOrderByFechaDescIdDesc(usuarioId);
    }

    public long contarNoLeidas(Long usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    /** Marca una como leída y devuelve su enlace (para redirigir al hacer clic). */
    @Transactional
    public String abrir(Long usuarioId, Long notificacionId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .filter(n -> n.getUsuario().getId().equals(usuarioId))
                .orElseThrow(() -> new NegocioException("La notificación no existe."));
        notificacion.setLeida(true);
        notificacionRepository.save(notificacion);
        return notificacion.getEnlace();
    }

    @Transactional
    public void marcarTodasLeidas(Long usuarioId) {
        notificacionRepository.marcarTodasLeidas(usuarioId);
    }
}
