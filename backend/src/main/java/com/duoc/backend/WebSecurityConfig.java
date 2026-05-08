package com.duoc.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
@Configuration
@Profile("default")
class WebSecurityConfig {

    @Autowired
    JWTAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {

        http
            // Deshabilitar CSRF para permitir pruebas de API (común en entornos de desarrollo de Duoc)
            .csrf(csrf -> csrf.disable())
            
            // Configuración centralizada de encabezados de seguridad (Hardening)
            .headers(headers -> headers
                // Soluciona Riesgo MEDIO: Cabecera CSP
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self'; " +
                        "style-src 'self'; " +
                        "img-src 'self' data:; " +
                        "font-src 'self'; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none';"
                    )
                )
                // Evita ataques de Clickjacking
                .frameOptions(frame -> frame.deny())
                
                // Soluciona el error de compilación previo de XXssConfig
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                
                // Soluciona Riesgo BAJO: Falta encabezado X-Content-Type-Options
                .contentTypeOptions(withDefaults())
                
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                
                // Actualizado para evitar el warning de deprecación
                .permissionsPolicy(permissions -> permissions
                    .policy("camera=(), microphone=(), geolocation=(), payment=()")
                )
            )
            // Control de acceso a las rutas del proyecto veterinario
            .authorizeHttpRequests(authz -> authz
                // Permite el login. Nota: Asegúrate de que el frontend use POST
                .requestMatchers(HttpMethod.POST, Constants.LOGIN_URL).permitAll()
                .requestMatchers(HttpMethod.GET, "/pets/**").permitAll()
                .anyRequest().authenticated()
            )
            // Filtro para validación de tokens JWT
            .addFilterAfter(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}