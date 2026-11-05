package com.booktruck.security;

import com.booktruck.repository.EmpresaRepository;
import com.booktruck.repository.TransportadorRepository;
import com.booktruck.service.VerificacionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    // Usa repositorios en vez de servicios para evitar una dependencia circular con SecurityConfig
    private final EmpresaRepository empresaRepository;
    private final TransportadorRepository transportadorRepository;
    private final VerificacionService verificacionService;

    public AuthenticationSuccessHandlerImpl(EmpresaRepository empresaRepository,
                                             TransportadorRepository transportadorRepository,
                                             VerificacionService verificacionService) {
        this.empresaRepository = empresaRepository;
        this.transportadorRepository = transportadorRepository;
        this.verificacionService = verificacionService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {

        Long usuarioId = ((CustomUserDetails) authentication.getPrincipal()).getUsuarioId();

        // Las cuentas sin verificar entran directo al perfil, donde se suben los documentos
        String destino = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> switch (a.getAuthority()) {
                    case "ROLE_EMPRESA" -> empresaRepository.findByUsuarioId(usuarioId)
                            .filter(verificacionService::empresaVerificada).isPresent()
                            ? "/empresa/inicio" : "/empresa/perfil";
                    case "ROLE_TRANSPORTADOR" -> transportadorRepository.findByUsuarioId(usuarioId)
                            .filter(verificacionService::transportadorVerificado).isPresent()
                            ? "/transportador/inicio" : "/transportador/perfil";
                    case "ROLE_ADMINISTRADOR" -> "/admin/dashboard";
                    default -> "/auth/login";
                })
                .orElse("/auth/login");

        response.sendRedirect(request.getContextPath() + destino);
    }
}
