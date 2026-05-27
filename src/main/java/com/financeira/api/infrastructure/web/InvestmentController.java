package com.financeira.api.infrastructure.web;

import com.financeira.api.application.dto.InvestmentRequest;
import com.financeira.api.application.dto.InvestmentResponse;
import com.financeira.api.application.usecase.investment.*;
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
@RequestMapping("/api/investments")
@Tag(name = "Investimentos", description = "Gerenciamento de investimentos. `type`: renda_fixa | renda_variavel | fundo | cripto | imovel | outro")
public class InvestmentController {

    private final CreateInvestmentUseCase create;
    private final UpdateInvestmentUseCase update;
    private final DeleteInvestmentUseCase delete;
    private final ListInvestmentsUseCase list;

    public InvestmentController(CreateInvestmentUseCase create, UpdateInvestmentUseCase update,
                                DeleteInvestmentUseCase delete, ListInvestmentsUseCase list) {
        this.create = create;
        this.update = update;
        this.delete = delete;
        this.list = list;
    }

    @GetMapping
    @Operation(summary = "Listar investimentos", description = "Equivale a `store.investments`.")
    public List<InvestmentResponse> listAll(Authentication auth) {
        return list.execute(uid(auth));
    }

    @PostMapping
    @Operation(
        summary = "Criar investimento",
        description = "`amount` = valor investido, `currentValue` = valor atual de mercado. `rate` em % a.a. (opcional).",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "Renda fixa", value = """
                        {
                          "name": "CDB Bradesco 110% CDI",
                          "type": "renda_fixa",
                          "amount": 10000.00,
                          "currentValue": 10620.00,
                          "rate": 13.2,
                          "maturity": "2026-12-01",
                          "bankId": "b2c3d4e5-0000-0000-0000-000000000001",
                          "isEmergencyReserve": false,
                          "icon": "📊",
                          "color": "#34d399"
                        }
                        """),
                    @ExampleObject(name = "Reserva de emergência", value = """
                        {
                          "name": "Reserva de Emergência",
                          "type": "renda_fixa",
                          "amount": 20000.00,
                          "currentValue": 20800.00,
                          "rate": 12.5,
                          "bankId": "b2c3d4e5-0000-0000-0000-000000000002",
                          "isEmergencyReserve": true,
                          "icon": "🛡️",
                          "color": "#7c8dff"
                        }
                        """)
                }))
    )
    public ResponseEntity<InvestmentResponse> create(@Valid @RequestBody InvestmentRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(create.execute(uid(auth), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar investimento", description = "Use para atualizar `currentValue` quando o valor de mercado mudar.")
    public InvestmentResponse update(
            @Parameter(description = "UUID do investimento") @PathVariable UUID id,
            @Valid @RequestBody InvestmentRequest request,
            Authentication auth) {
        return update.execute(uid(auth), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir investimento (soft delete)")
    @ApiResponse(responseCode = "204", description = "Excluído com sucesso")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID do investimento") @PathVariable UUID id,
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
