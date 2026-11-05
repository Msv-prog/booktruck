package com.booktruck.service;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.Documento;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class DocumentoService {

    private final FileStorageService fileStorageService;

    public DocumentoService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /** Archivo de un documento de verificación; 404 si no existe en vez de un error. */
    public ResponseEntity<Resource> servir(Optional<Documento> documento) {
        if (documento.isEmpty() || !Files.exists(fileStorageService.resolver(documento.get().getArchivoRuta()))) {
            return ResponseEntity.notFound().build();
        }
        return servir(documento.get().getArchivoRuta(), documento.get().getNombreOriginal());
    }

    public ResponseEntity<Resource> servir(String nombreArchivo, String nombreOriginal) {
        if (nombreArchivo == null) {
            throw new NegocioException("No hay ningún documento subido.");
        }
        Path ruta = fileStorageService.resolver(nombreArchivo);
        if (!Files.exists(ruta)) {
            throw new NegocioException("El documento ya no está disponible.");
        }

        Resource recurso;
        try {
            recurso = new UrlResource(ruta.toUri());
        } catch (MalformedURLException e) {
            throw new NegocioException("No fue posible abrir el documento.");
        }

        String tipoContenido;
        try {
            tipoContenido = Files.probeContentType(ruta);
        } catch (IOException e) {
            tipoContenido = null;
        }
        if (tipoContenido == null) {
            tipoContenido = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String nombreDescarga = nombreOriginal != null ? nombreOriginal : nombreArchivo;
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(tipoContenido))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreDescarga + "\"")
                .body(recurso);
    }
}
