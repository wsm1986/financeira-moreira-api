package com.financeira.api.infrastructure.web;

import com.financeira.api.application.dto.ImportPortalRequest;
import com.financeira.api.application.dto.ImportPortalResponse;
import com.financeira.api.application.usecase.importportal.ImportPortalUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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

            **Ordem de importação:**
            1. categories (idempotente — skip se nome já existe)
            2. banks
            3. credit_cards
            4. recurrences (antes das entries para resolver recurrenceId)
            5. entries (com mapeamento de installmentGroupId e recurrenceId)
            6. bills
            7. investments
            8. goals
            9. payslips + payslip_items

            **Formatos aceitos:**

            **Formato 1 — com wrapper `state` (exportado direto do localStorage):**
            ```json
            { "state": { "entries": [...], "banks": [...], ... }, "version": 1 }
            ```

            **Formato 2 — sem wrapper (JSON direto):**
            ```json
            { "entries": [...], "banks": [...], "cards": [...], ... }
            ```

            **Comportamento:**
            - Categorias duplicadas (mesmo nome, case-insensitive) são ignoradas com `skipped++`
            - Entries com categoria inexistente geram warning e são ignoradas
            - Toda operação é em **transação única** — qualquer erro faz rollback total
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "Formato com wrapper state", value = """
                        {
                          "state": {
                            "categories": [
                              { "id": "cat1", "name": "Alimentação", "icon": "🍔", "budget": 1200, "color": "#7c8dff", "type": "expense", "nature": "essencial" },
                              { "id": "cat2", "name": "Renda", "icon": "💼", "budget": 0, "color": "#34d399", "type": "income" }
                            ],
                            "banks": [
                              { "id": "b1", "name": "Bradesco", "type": "corrente", "balance": 12450.00, "color": "#e63946", "icon": "🏦" }
                            ],
                            "cards": [
                              { "id": "c1", "name": "Nubank", "brand": "mastercard", "lastDigits": "1234", "limit": 8000, "closingDay": 3, "dueDay": 10, "color": "#8a05be", "icon": "💜", "bankId": null }
                            ],
                            "entries": [
                              { "id": "e1", "monthKey": "2026-05", "kind": "debito_avista", "name": "Supermercado", "category": "Alimentação", "amount": 350, "date": "2026-05-10", "icon": "🛒", "accountId": "b1", "isPaid": true }
                            ],
                            "recurrences": [],
                            "bills": [],
                            "investments": [],
                            "goals": [],
                            "payslips": [],
                            "config": { "currentMonthKey": "2026-05", "userName": "Wellington", "currency": "BRL" }
                          }
                        }
                        """),
                    @ExampleObject(name = "Formato direto (sem wrapper)", value = """
                        {
                          "categories": [
                            { "id": "cat1", "name": "Alimentação", "icon": "🍔", "budget": 1200, "color": "#7c8dff", "type": "expense" }
                          ],
                          "banks": [
                            { "id": "b1", "name": "Nubank", "type": "digital", "balance": 1800.00, "color": "#8a05be", "icon": "💜" }
                          ],
                          "cards": [],
                          "entries": [],
                          "recurrences": [],
                          "bills": [],
                          "investments": [],
                          "goals": [],
                          "payslips": []
                        }
                        """)
                })),
        responses = {
            @ApiResponse(responseCode = "200", description = "Importação concluída (com ou sem warnings)",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = """
                        {
                          "summary": {
                            "categories":  { "imported": 9,   "skipped": 1 },
                            "banks":       { "imported": 4,   "skipped": 0 },
                            "cards":       { "imported": 2,   "skipped": 0 },
                            "recurrences": { "imported": 5,   "skipped": 0 },
                            "entries":     { "imported": 350, "skipped": 2 },
                            "bills":       { "imported": 8,   "skipped": 0 },
                            "investments": { "imported": 4,   "skipped": 0 },
                            "goals":       { "imported": 4,   "skipped": 0 },
                            "payslips":    { "imported": 3,   "skipped": 0 }
                          },
                          "warnings": [
                            "Categoria 'Transferências' não encontrada, lançamento 'TED Nubank' ignorado"
                          ]
                        }
                        """))),
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
