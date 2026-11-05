package com.booktruck.config;

import com.booktruck.security.AuthenticationSuccessHandlerImpl;
import com.booktruck.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final AuthenticationSuccessHandlerImpl successHandler;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                           AuthenticationSuccessHandlerImpl successHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.successHandler = successHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider =
            new DaoAuthenticationProvider(customUserDetailsService);

    provider.setPasswordEncoder(passwordEncoder());

    // Primero se valida la contraseña y después si la cuenta está desactivada (así no se revela qué correos existen)
    provider.setPreAuthenticationChecks(usuario -> { });
    provider.setPostAuthenticationChecks(usuario -> {
        if (!usuario.isEnabled()) {
            throw new DisabledException("Cuenta desactivada");
        }
    });

    return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/terminos", "/privacidad", "/error", "/auth/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/empresa/**").hasRole("EMPRESA")
                .requestMatchers("/transportador/**").hasRole("TRANSPORTADOR")
                .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureHandler((request, response, exception) -> response.sendRedirect(
                        request.getContextPath() + (exception instanceof DisabledException
                                ? "/auth/login?desactivada" : "/auth/login?error")))
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/auth/login?logout")
                .permitAll()
            )
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}