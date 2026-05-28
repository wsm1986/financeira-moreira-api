package com.financeira.api.infrastructure.web;

import com.financeira.api.application.dto.EntryRequest;
import com.financeira.api.application.dto.EntryResponse;
import com.financeira.api.application.usecase.entry.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    Lançamentos financeiros mensais. `kind` suportados:
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
        description = "Retorna todos os lançamentos do mês. Equivale a `selectEntriesByMonth(monthKey)` no portal.",
        parameters = @Parameter(name = "monthKey", description = "Mês no formato YYYY-MM", required = true, example = "2026-05")
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
            Campos obrigatórios por `kind`:
            - **receita / debito_avista / debito_recorrente** → `accountId`
            - **credito_avista** → `cardId`
            - **credito_parcelado** → `cardId`, `installmentTotal`, `installmentCurrent`, `installmentGroupId`
            - **recorrente_cartao** → `cardId`, `recurrenceId`
            - **pagamento_fatura** → `accountId`, `cardId`, `invoiceRef` (YYYY-MM)
            - **transferencia** → `accountId` (origem), `toAccountId` (destino)
            """,
        responses = {
            @ApiResponse(responseCode = "201", description = "Lançamento criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
        }
    )
    public ResponseEntity<EntryResponse> create(@Valid @RequestBody EntryRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(create.execute(uid(auth), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar lançamento")
    public EntryResponse update(
            @Parameter(description = "UUID do lançamento") @PathVariable UUID id,
            @Valid @RequestBody EntryRequest request,
            Authentication auth) {
        return update.execute(uid(auth), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir lançamento (soft delete)", description = "Equivale ao `softDeleteEntry` do portal.")
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
