package com.financeira.api.infrastructure.web;

import com.financeira.api.application.dto.CardRequest;
import com.financeira.api.application.dto.CardResponse;
import com.financeira.api.application.usecase.card.*;
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
@RequestMapping("/api/cards")
@Tag(name = "Cartões de Crédito", description = "Gerenciamento de cartões. `brand`: visa | mastercard | elo | amex | hipercard")
public class CardController {

    private final CreateCardUseCase create;
    private final UpdateCardUseCase update;
    private final DeleteCardUseCase delete;
    private final ListCardsUseCase list;

    public CardController(CreateCardUseCase create, UpdateCardUseCase update,
                          DeleteCardUseCase delete, ListCardsUseCase list) {
        this.create = create;
        this.update = update;
        this.delete = delete;
        this.list = list;
    }

    @GetMapping
    @Operation(summary = "Listar cartões", description = "Equivale a `store.cards` no portal.")
    public List<CardResponse> listAll(Authentication auth) {
        return list.execute(uid(auth));
    }

    @PostMapping
    @Operation(
        summary = "Criar cartão",
        description = "`closingDay` e `dueDay` entre 1 e 28. `cardLimit` é o limite total do cartão.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "name": "Nubank",
                      "brand": "mastercard",
                      "lastDigits": "1234",
                      "cardLimit": 8000.00,
                      "closingDay": 3,
                      "dueDay": 10,
                      "color": "#8a05be",
                      "icon": "💜",
                      "bankId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
                    }
                    """)))
    )
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CardRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(create.execute(uid(auth), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cartão")
    public CardResponse update(
            @Parameter(description = "UUID do cartão") @PathVariable UUID id,
            @Valid @RequestBody CardRequest request,
            Authentication auth) {
        return update.execute(uid(auth), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cartão (soft delete)")
    @ApiResponse(responseCode = "204", description = "Excluído com sucesso")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID do cartão") @PathVariable UUID id,
            Authentication auth) {
        delete.execute(uid(auth), id);
        return ResponseEntity.noContent().build();
    }

    private String uid(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getUsername();
        }
        return String.valueOf(principal);
    }
}
