package com.financeira.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configura CORS via CorsConfigurationSource — compatível com Spring Security's
 * .cors(Customizer.withDefaults()), que procura este bean pelo tipo.
 *
 * O filtro CORS do Security roda ANTES dos filtros de autenticação, garantindo
 * que preflights OPTIONS nunca recebam 401/403.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Permite qualquer origem (portal local em :5173, Vercel, Netlify…)
        config.setAllowedOriginPatterns(List.of("*"));

        // Inclui PATCH para futuras operações parciais
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Todos os headers — inclui Authorization, X-Dev-User-Id, Content-Type
        config.setAllowedHeaders(List.of("*"));

        // Headers acessíveis ao JS do cliente
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));

        // Sem cookies — compatível com allowedOriginPatterns("*")
        config.setAllowCredentials(false);

        // Cache do preflight OPTIONS por 1 hora
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
