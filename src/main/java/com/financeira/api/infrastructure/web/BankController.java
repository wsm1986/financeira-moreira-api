package com.financeira.api.infrastructure.web;

import com.financeira.api.application.dto.BankRequest;
import com.financeira.api.application.dto.BankResponse;
import com.financeira.api.application.usecase.bank.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/banks")
@Tag(name = "Bancos / Contas", description = "Gerenciamento de contas bancárias (corrente, poupança, digital, investimento)")
public class BankController {

    private final CreateBankUseCase create;
    private final UpdateBankUseCase update;
    private final DeleteBankUseCase delete;
    private final ListBanksUseCase list;
    private final AdjustBankBalanceUseCase adjustBalance;

    public BankController(CreateBankUseCase create, UpdateBankUseCase update,
                          DeleteBankUseCase delete, ListBanksUseCase list,
                          AdjustBankBalanceUseCase adjustBalance) {
        this.create = create;
        this.update = update;
        this.delete = delete;
        this.list = list;
        this.adjustBalance = adjustBalance;
    }

    @GetMapping
    @Operation(
        summary = "Listar contas",
        description = "Retorna todas as contas ativas do usuário autenticado. Equivale a `store.banks` no portal.",
        responses = @ApiResponse(responseCode = "200", description = "Lista de contas",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    [
                      {
                        "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                        "name": "Bradesco",
                        "type": "corrente",
                        "balance": 12450.00,
                        "color": "#e63946",
                        "icon": "🏦"
                      }
                    ]
                    """)))
    )
    public List<BankResponse> listAll(Authentication auth) {
        return list.execute(uid(auth));
    }

    @PostMapping
    @Operation(
        summary = "Criar conta",
        description = "Cria nova conta bancária. `type` deve ser: `corrente | poupanca | investimento | digital`.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "name": "Nubank",
                      "type": "digital",
                      "balance": 1800.00,
                      "color": "#8a05be",
                      "icon": "💜"
                    }
                    """))),
        responses = {
            @ApiResponse(responseCode = "201", description = "Conta criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
        }
    )
    public ResponseEntity<BankResponse> create(@Valid @RequestBody BankRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(create.execute(uid(auth), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar conta", description = "Atualiza nome, tipo, saldo, cor ou ícone da conta.")
    public BankResponse update(
            @Parameter(description = "UUID da conta") @PathVariable UUID id,
            @Valid @RequestBody BankRequest request,
            Authentication auth) {
        return update.execute(uid(auth), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir conta (soft delete)", description = "Marca a conta como excluída (deleted_at). Lançamentos vinculados são preservados.")
    @ApiResponse(responseCode = "204", description = "Excluída com sucesso")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID da conta") @PathVariable UUID id,
            Authentication auth) {
        delete.execute(uid(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/balance")
    @Operation(
        summary = "Ajustar saldo",
        description = "Sobrescreve o saldo da conta diretamente (usado para reconciliação manual com extrato).",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    { "balance": 15200.00 }
                    """)))
    )
    public BankResponse adjustBalance(
            @Parameter(description = "UUID da conta") @PathVariable UUID id,
            @RequestBody Map<String, BigDecimal> body,
            Authentication auth) {
        return adjustBalance.execute(uid(auth), id, body.get("balance"));
    }

    private String uid(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getUsername();
        }
        return String.valueOf(principal);
    }
}
