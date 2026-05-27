package com.financeira.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Financeira Moreira API")
                        .description("""
                                API REST da Financeira Moreira — gerenciamento financeiro pessoal.

                                ## Autenticação
                                Todos os endpoints (exceto `/api/ping` e `/api/import`) exigem token Bearer.

                                **Dev:** use `Authorization: Bearer dev-token` (qualquer valor funciona com `app.auth.mode=dev`).

                                **Prod:** use o ID Token Firebase obtido no login do portal.

                                ## Compatibilidade com o Portal
                                Os campos seguem exatamente os tipos definidos em `src/types/index.ts` do portal React.
                                Datas no formato `YYYY-MM-DD`, meses no formato `YYYY-MM`.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Wellington Moreira")
                                .email("wellington@financeira-moreira.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Dev (H2)"),
                        new Server().url("https://financeira-moreira-api.onrender.com").description("Prod (Neon PostgreSQL)")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("Firebase ID Token | 'dev-token'")
                                .description("Firebase ID Token (prod) ou qualquer valor em modo dev")));
    }
}
