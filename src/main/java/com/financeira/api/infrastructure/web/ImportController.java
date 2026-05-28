package com.financeira.api.infrastructure.web;

import com.financeira.api.application.dto.ImportPortalRequest;
import com.financeira.api.application.dto.ImportPortalResponse;
import com.financeira.api.application.usecase.importportal.ImportPortalUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/import")
@Tag(name = "Migração / Import", description = "Importa dados do portal (JSON exportado pelo Zustand persist) para o banco de dados PostgreSQL")
public class ImportController {

    private final ImportPortalUseCase importUseCase;

    public ImportController(ImportPortalUseCase importUseCase) {
        this.importUseCase = importUseCase;
    }

    @PostMapping
    @Operation(
        summary = "Importar dados do portal",
        description = """
            Recebe o JSON exportado pelo portal (`Configurações → Exportar dados`) e popula todas as tabelas.

            **Ordem de importação:** categories → banks → credit_cards → recurrences → entries → bills → investments → goals → payslips

            **Formatos aceitos:**
            - Com wrapper `state`: `{ "state": { "entries": [...], ... }, "version": 1 }`
            - Direto: `{ "entries": [...], "banks": [...], ... }`

            **Comportamento:**
            - Categorias duplicadas (mesmo nome, case-insensitive) são ignoradas com `skipped++`
            - Entries com categoria inexistente geram warning e são ignoradas
            - Toda operação é em **transação única** — qualquer erro faz rollback total
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Importação concluída (com ou sem warnings)"),
            @ApiResponse(responseCode = "500", description = "Erro na importação — rollback total executado")
        }
    )
    public ResponseEntity<ImportPortalResponse> importPortal(
            @RequestBody ImportPortalRequest request,
            Authentication auth) {
        return ResponseEntity.ok(importUseCase.execute(uid(auth), request));
    }

    private String uid(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getUsername();
        }
        return String.valueOf(principal);
    }
}
