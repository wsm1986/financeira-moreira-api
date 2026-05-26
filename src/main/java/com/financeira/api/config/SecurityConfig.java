package com.financeira.api.config;

import com.financeira.api.infrastructure.security.DevAuthFilter;
import com.financeira.api.infrastructure.security.FirebaseAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.auth.mode:dev}")
    private String authMode;

    private static final String[] PUBLIC_PATHS = {
            "/api/sync",
            "/h2-console/**",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS usando o bean CorsConfigurationSource registrado em CorsConfig
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .anyRequest().authenticated()
            )
            // Retorna 401 com JSON body (o portal exibe o body no response panel)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write(
                        "{\"error\":\"UNAUTHORIZED\",\"message\":\"Token ausente ou inválido — configure Firebase ou use auth dev\"}"
                    );
                })
            );

        if ("dev".equals(authMode)) {
            http.addFilterBefore(new DevAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        } else {
            http.addFilterBefore(new FirebaseAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        }

        // H2 console precisa de frames no dev
        if ("dev".equals(authMode)) {
            http.headers(h -> h.frameOptions(f -> f.sameOrigin()));
        }

        return http.build();
    }
}
