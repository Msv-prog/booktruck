package com.booktruck.service;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.Usuario;
import com.booktruck.repository.UsuarioRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/** Foto de perfil del transportador o logo de la empresa. Sin foto se muestran las iniciales. */
@Service
public class FotoPerfilService {

    private final UsuarioRepository usuarioRepository;
    private final FileStorageService fileStorageService;

    public FotoPerfilService(UsuarioRepository usuarioRepository, FileStorageService fileStorageService) {
        this.usuarioRepository = usuarioRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public void guardar(Usuario usuario, MultipartFile foto) {
        String anterior = usuario.getFotoRuta();
        usuario.setFotoRuta(fileStorageService.guardarImagen(foto, "usuario_" + usuario.getId()));
        usuarioRepository.save(usuario);
        fileStorageService.eliminarFoto(anterior);
    }

    @Transactional
    public void quitar(Usuario usuario) {
        String anterior = usuario.getFotoRuta();
        usuario.setFotoRuta(null);
        usuarioRepository.save(usuario);
        fileStorageService.eliminarFoto(anterior);
    }

    /** El admin quita una foto inapropiada; devuelve el usuario para la auditoría. */
    @Transactional
    public Usuario quitarComoAdmin(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NegocioException("El usuario no existe."));
        if (usuario.getFotoRuta() == null) {
            throw new NegocioException("Ese usuario no tiene foto.");
        }
        quitar(usuario);
        return usuario;
    }

    /** Sirve la imagen (solo a usuarios con sesión iniciada); 404 si no tiene foto. */
    public ResponseEntity<Resource> servir(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null || usuario.getFotoRuta() == null) {
            return ResponseEntity.notFound().build();
        }
        Path ruta = fileStorageService.resolverFoto(usuario.getFotoRuta());
        if (!Files.exists(ruta)) {
            return ResponseEntity.notFound().build();
        }
        try {
            MediaType tipo = usuario.getFotoRuta().endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
            return ResponseEntity.ok()
                    .contentType(tipo)
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                    .body(new UrlResource(ruta.toUri()));
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
