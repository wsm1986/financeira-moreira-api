package com.financeira.api.infrastructure.web;

import com.financeira.api.application.dto.EntryRequest;
import com.financeira.api.application.dto.EntryResponse;
import com.financeira.api.application.usecase.entry.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/entries")
@Tag(name = "Lançamentos", description = """
    Lançamentos financeiros mensais. Tipos suportados (kind):
    receita | debito_avista | debito_recorrente | credito_avista |
    credito_parcelado | recorrente_cartao | pagamento_fatura | transferencia
    """)
public class EntryController {

    private final CreateEntryUseCase create;
    private final UpdateEntryUseCase update;
    private final DeleteEntryUseCase delete;
    private final ListEntriesByMonthUseCase listByMonth;
    private final ToggleEntryPaidUseCase togglePaid;

    public EntryController(CreateEntryUseCase create, UpdateEntryUseCase update,
                           DeleteEntryUseCase delete, ListEntriesByMonthUseCase listByMonth,
                           ToggleEntryPaidUseCase togglePaid) {
        this.create = create;
        this.update = update;
        this.delete = delete;
        this.listByMonth = listByMonth;
        this.togglePaid = togglePaid;
    }

    @GetMapping
    @Operation(
        summary = "Listar lançamentos por mês",
        description = "Retorna todos os lançamentos do mês informado. Equivale a `selectEntriesByMonth(monthKey)` no portal.",
        parameters = @Parameter(name = "monthKey", description = "Mês no formato YYYY-MM (ex: 2026-05)", required = true, example = "2026-05"),
        responses = @ApiResponse(responseCode = "200", description = "Lista de lançamentos do mês",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    [
                      {
                        "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                        "monthKey": "2026-05",
                        "kind": "debito_avista",
                        "name": "Supermercado",
                        "categoryId": "a1b2c3d4-0000-0000-0000-000000000001",
                        "amount": 350.00,
                        "entryDate": "2026-05-10",
                        "icon": "🛒",
                        "accountId": "b2c3d4e5-0000-0000-0000-000000000001",
                        "isPaid": true,
                        "isReconciled": false
                      }
                    ]
                    """)))
    )
    public List<EntryResponse> listByMonth(
            @RequestParam String monthKey,
            Authentication auth) {
        return listByMonth.execute(uid(auth), monthKey);
    }

    @PostMapping
    @Operation(
        summary = "Criar lançamento",
        description = """
            Cria um lançamento financeiro. Regras por `kind`:

            - **receita** → `accountId` obrigatório
            - **debito_avista / debito_recorrente** → `accountId` obrigatório
            - **credito_avista** → `cardId` obrigatório
            - **credito_parcelado** → `cardId` + `installmentTotal` + `installmentCurrent` + `installmentGroupId` obrigatórios
            - **recorrente_cartao** → `cardId` + `recurrenceId` obrigatórios
            - **pagamento_fatura** → `accountId` + `cardId` + `invoiceRef` (YYYY-MM) obrigatórios
            - **transferencia** → `accountId` (origem) + `toAccountId` (destino) obrigatórios
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "Débito à vista", value = """
                        {
                          "monthKey": "2026-05",
                          "kind": "debito_avista",
                          "name": "Supermercado",
                          "categoryId": "a1b2c3d4-0000-0000-0000-000000000001",
                          "amount": 350.00,
                          "entryDate": "2026-05-10",
                          "icon": "🛒",
                          "accountId": "b2c3d4e5-0000-0000-0000-000000000001",
                          "isPaid": true
                        }
                        """),
                    @ExampleObject(name = "Crédito parcelado (3x)", value = """
                        {
                          "monthKey": "2026-06",
                          "kind": "credito_parcelado",
                          "name": "iPhone 16 — 1/3",
                          "categoryId": "a1b2c3d4-0000-0000-0000-000000000002",
                          "amount": 1400.00,
                          "entryDate": "2026-06-08",
                          "icon": "📱",
                          "cardId": "c3d4e5f6-0000-0000-0000-000000000001",
                          "billingMonth": "2026-06",
                          "installmentTotal": 3,
                          "installmentCurrent": 1,
                          "installmentGroupId": "d4e5f6a7-0000-0000-0000-000000000001"
                        }
                        """),
                    @ExampleObject(name = "Pagamento de fatura", value = """
                        {
                          "monthKey": "2026-06",
                          "kind": "pagamento_fatura",
                          "name": "Pagamento Nubank Jun/26",
                          "categoryId": "a1b2c3d4-0000-0000-0000-000000000003",
                          "amount": 1455.90,
                          "entryDate": "2026-06-10",
                          "icon": "💳",
                          "accountId": "b2c3d4e5-0000-0000-0000-000000000001",
                          "cardId": "c3d4e5f6-0000-0000-0000-000000000001",
                          "invoiceRef": "2026-06",
                          "isPaid": true
                        }
                        """),
                    @ExampleObject(name = "Transferência entre contas", value = """
                        {
                          "monthKey": "2026-05",
                          "kind": "transferencia",
                          "name": "TED para poupança",
                          "categoryId": "a1b2c3d4-0000-0000-0000-000000000004",
                          "amount": 500.00,
                          "entryDate": "2026-05-15",
                          "icon": "↔️",
                          "accountId": "b2c3d4e5-0000-0000-0000-000000000001",
                          "toAccountId": "b2c3d4e5-0000-0000-0000-000000000002"
                        }
                        """),
                    @ExampleObject(name = "Receita (salário)", value = """
                        {
                          "monthKey": "2026-05",
                          "kind": "receita",
                          "name": "Salário",
                          "categoryId": "a1b2c3d4-0000-0000-0000-000000000005",
                          "amount": 8500.00,
                          "entryDate": "2026-05-05",
                          "icon": "💵",
                          "accountId": "b2c3d4e5-0000-0000-0000-000000000001",
                          "isPaid": true
                        }
                        """)
                })),
        responses = {
            @ApiResponse(responseCode = "201", description = "Lançamento criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (monthKey, kind ou amount)")
        }
    )
    public ResponseEntity<EntryResponse> create(@Valid @RequestBody EntryRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(create.execute(uid(auth), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar lançamento", description = "Atualiza todos os campos de um lançamento existente.")
    public EntryResponse update(
            @Parameter(description = "UUID do lançamento") @PathVariable UUID id,
            @Valid @RequestBody EntryRequest request,
            Authentication auth) {
        return update.execute(uid(auth), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir lançamento (soft delete)", description = "Marca o lançamento como excluído. Equivale ao `softDeleteEntry` do portal.")
    @ApiResponse(responseCode = "204", description = "Excluído com sucesso")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID do lançamento") @PathVariable UUID id,
            Authentication auth) {
        delete.execute(uid(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/paid")
    @Operation(summary = "Alternar pago/pendente", description = "Inverte o campo `isPaid`. Equivale ao `togglePaid` do portal.")
    public EntryResponse togglePaid(
            @Parameter(description = "UUID do lançamento") @PathVariable UUID id,
            Authentication auth) {
        return togglePaid.execute(uid(auth), id);
    }

    private String uid(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getUsername();
        }
        return String.valueOf(principal);
    }
}
