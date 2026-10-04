package com.miagenda.config;

import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(AdminProperties admin, PasswordEncoder passwordEncoder) {
        if (admin.username() == null || admin.username().isBlank()
                || admin.password() == null || admin.password().isBlank()) {
            throw new IllegalStateException(
                    "Define las variables de entorno ADMIN_USERNAME y ADMIN_PASSWORD.");
        }
        UserDetails adminUser = User.withUsername(admin.username())
                .password(admin.password())
                .passwordEncoder(passwordEncoder::encode)
                .authorities("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(adminUser);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        LoginUrlAuthenticationEntryPoint loginEntryPoint = new LoginUrlAuthenticationEntryPoint("/login");
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/error", "/styles.css", "/app.js", "/favicon.ico").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/views/agenda", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    String uri = request.getRequestURI();
                    if (uri != null && (uri.equals("/agenda") || uri.startsWith("/agenda/"))) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                        response.getWriter().write("{\"detail\":\"No autenticado\"}");
                        return;
                    }
                    loginEntryPoint.commence(request, response, authException);
                }).accessDeniedHandler((request, response, accessDeniedException) -> {
                    String uri = request.getRequestURI();
                    if (uri != null && (uri.equals("/agenda") || uri.startsWith("/agenda/"))) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                        response.getWriter().write("{\"detail\":\"No se pudo validar la solicitud\"}");
                        return;
                    }
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }));
        return http.build();
    }
}
