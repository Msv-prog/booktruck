package com.booktruck.service;

import com.booktruck.exception.NegocioException;
import com.booktruck.model.Usuario;
import com.booktruck.model.enums.RolUsuario;
import com.booktruck.repository.UsuarioRepository;
import com.booktruck.util.Textos;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAllByOrderByFechaCreacionDesc();
    }

    // Filtros del admin (null = sin filtro)
    public List<Usuario> filtrar(List<Usuario> usuarios, RolUsuario rol, Boolean activo, String correo) {
        return usuarios.stream()
                .filter(u -> rol == null || u.getRol() == rol)
                .filter(u -> activo == null || u.getActivo().equals(activo))
                .filter(u -> Textos.coincide(correo, u.getEmail()))
                .toList();
    }

    @Transactional
    public Usuario alternarActivo(Long usuarioId, Long usuarioActualId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NegocioException("El usuario no existe."));
        if (usuario.getId().equals(usuarioActualId)) {
            throw new NegocioException("No puedes desactivar tu propia cuenta.");
        }
        usuario.setActivo(!usuario.getActivo());
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva, String confirmarPasswordNueva) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NegocioException("El usuario no existe."));

        if (passwordActual == null || !passwordEncoder.matches(passwordActual, usuario.getPasswordHash())) {
            throw new NegocioException("La contraseña actual no es correcta.");
        }
        if (passwordNueva == null || passwordNueva.length() < 6) {
            throw new NegocioException("La nueva contraseña debe tener al menos 6 caracteres.");
        }
        if (!passwordNueva.equals(confirmarPasswordNueva)) {
            throw new NegocioException("Las contraseñas nuevas no coinciden.");
        }

        usuario.setPasswordHash(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void desactivarCuentaPropia(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NegocioException("El usuario no existe."));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario crearUsuarioBase(String email, String password, String confirmarPassword, RolUsuario rol) {
        if (email == null || email.isBlank()) {
            throw new NegocioException("El correo es obligatorio.");
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new NegocioException("El correo no tiene un formato válido.");
        }
        if (password == null || password.length() < 6) {
            throw new NegocioException("La contraseña debe tener al menos 6 caracteres.");
        }
        if (!password.equals(confirmarPassword)) {
            throw new NegocioException("Las contraseñas no coinciden.");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new NegocioException("Ya existe una cuenta registrada con ese correo.");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }
}