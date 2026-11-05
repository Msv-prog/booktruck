package com.booktruck.service;

import com.booktruck.exception.NegocioException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final List<String> EXTENSIONES_PERMITIDAS = List.of("pdf", "jpg", "jpeg", "png");
    private static final long TAMANO_MAXIMO_BYTES = 5L * 1024 * 1024;

    private final Path directorioBase = Paths.get("uploads", "documentos").toAbsolutePath().normalize();

    // Fotos de perfil y logos: solo imágenes y más livianas, en su propia carpeta
    private static final List<String> EXTENSIONES_IMAGEN = List.of("jpg", "jpeg", "png");
    private static final long TAMANO_MAXIMO_IMAGEN = 2L * 1024 * 1024;
    private final Path directorioFotos = Paths.get("uploads", "fotos").toAbsolutePath().normalize();

    public String guardarImagen(MultipartFile archivo, String prefijo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new NegocioException("Debes seleccionar una imagen.");
        }
        if (archivo.getSize() > TAMANO_MAXIMO_IMAGEN) {
            throw new NegocioException("La imagen no puede superar los 2 MB.");
        }
        String extension = extraerExtension(archivo.getOriginalFilename());
        String tipo = archivo.getContentType();
        if (!EXTENSIONES_IMAGEN.contains(extension) || tipo == null || !tipo.startsWith("image/")) {
            throw new NegocioException("Solo se permiten imágenes JPG o PNG.");
        }
        try {
            Files.createDirectories(directorioFotos);
            String nombreArchivo = prefijo + "_" + UUID.randomUUID() + "." + extension;
            Files.copy(archivo.getInputStream(), directorioFotos.resolve(nombreArchivo), StandardCopyOption.REPLACE_EXISTING);
            return nombreArchivo;
        } catch (IOException e) {
            throw new NegocioException("No fue posible guardar la imagen. Intenta de nuevo.");
        }
    }

    public Path resolverFoto(String nombreArchivo) {
        return directorioFotos.resolve(nombreArchivo).normalize();
    }

    public void eliminarFoto(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return;
        }
        Path ruta = resolverFoto(nombreArchivo);
        if (!ruta.startsWith(directorioFotos)) {
            return;
        }
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException e) {
            // Queda un archivo sin uso; no impide cambiar la foto
        }
    }

    public String guardar(MultipartFile archivo, String prefijo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new NegocioException("Debes seleccionar un archivo.");
        }
        if (archivo.getSize() > TAMANO_MAXIMO_BYTES) {
            throw new NegocioException("El archivo no puede superar los 5 MB.");
        }

        String nombreOriginal = archivo.getOriginalFilename();
        String extension = extraerExtension(nombreOriginal);
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new NegocioException("Solo se permiten archivos PDF, JPG o PNG.");
        }

        try {
            Files.createDirectories(directorioBase);
            String nombreArchivo = prefijo + "_" + UUID.randomUUID() + "." + extension;
            Path destino = directorioBase.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return nombreArchivo;
        } catch (IOException e) {
            throw new NegocioException("No fue posible guardar el archivo. Intenta de nuevo.");
        }
    }

    public Path resolver(String nombreArchivo) {
        return directorioBase.resolve(nombreArchivo).normalize();
    }

    /** Borra un archivo reemplazado. Si no existe o falla, no interrumpe la operación principal. */
    public void eliminar(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return;
        }
        Path ruta = resolver(nombreArchivo);
        if (!ruta.startsWith(directorioBase)) {
            return;
        }
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException e) {
            // Queda un archivo sin uso en disco; no es motivo para fallar la subida del nuevo
        }
    }

    private String extraerExtension(String nombreOriginal) {
        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            return "";
        }
        return nombreOriginal.substring(nombreOriginal.lastIndexOf('.') + 1).toLowerCase();
    }
}
