package com.financeira.api.infrastructure.web;

import com.financeira.api.application.dto.RecurrenceRequest;
import com.financeira.api.application.dto.RecurrenceResponse;
import com.financeira.api.application.usecase.recurrence.*;
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
@RequestMapping("/api/recurrences")
@Tag(name = "Recorrências", description = "Contratos de lançamentos recorrentes. `active=false` significa cancelado — entradas existentes são preservadas.")
public class RecurrenceController {

    private final CreateRecurrenceUseCase create;
    private final UpdateRecurrenceUseCase update;
    private final DeleteRecurrenceUseCase delete;
    private final ListRecurrencesUseCase list;
    private final CancelRecurrenceUseCase cancel;

    public RecurrenceController(CreateRecurrenceUseCase create, UpdateRecurrenceUseCase update,
                                DeleteRecurrenceUseCase delete, ListRecurrencesUseCase list,
                                CancelRecurrenceUseCase cancel) {
        this.create = create;
        this.update = update;
        this.delete = delete;
        this.list = list;
        this.cancel = cancel;
    }

    @GetMapping
    @Operation(summary = "Listar recorrências", description = "Equivale a `store.recurrences` no portal.")
    public List<RecurrenceResponse> listAll(Authentication auth) {
        return list.execute(uid(auth));
    }

    @PostMapping
    @Operation(
        summary = "Criar contrato de recorrência",
        description = "Cria o contrato. As entradas mensais devem ser criadas separadamente via `POST /api/entries` com `recurrenceId` apontando para este contrato.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "name": "Netflix",
                      "icon": "📺",
                      "categoryId": "a1b2c3d4-0000-0000-0000-000000000001",
                      "kind": "recorrente_cartao",
                      "amount": 55.90,
                      "cardId": "c3d4e5f6-0000-0000-0000-000000000001",
                      "startMonth": "2026-01",
                      "endMonth": "2026-12",
                      "months": 12,
                      "active": true
                    }
                    """)))
    )
    public ResponseEntity<RecurrenceResponse> create(@Valid @RequestBody RecurrenceRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(create.execute(uid(auth), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar recorrência")
    public RecurrenceResponse update(
            @Parameter(description = "UUID da recorrência") @PathVariable UUID id,
            @Valid @RequestBody RecurrenceRequest request,
            Authentication auth) {
        return update.execute(uid(auth), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir recorrência (soft delete)")
    @ApiResponse(responseCode = "204", description = "Excluída com sucesso")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID da recorrência") @PathVariable UUID id,
            Authentication auth) {
        delete.execute(uid(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
        summary = "Cancelar recorrência",
        description = "Define `active=false`. As entradas já geradas são preservadas. Equivale ao `cancelRecurrence` do portal."
    )
    public RecurrenceResponse cancel(
            @Parameter(description = "UUID da recorrência") @PathVariable UUID id,
            Authentication auth) {
        return cancel.execute(uid(auth), id);
    }

    private String uid(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getUsername();
        }
        return String.valueOf(principal);
    }
}
