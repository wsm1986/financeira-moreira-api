package com.financeira.api.infrastructure.web;

import com.financeira.api.application.dto.BillRequest;
import com.financeira.api.application.dto.BillResponse;
import com.financeira.api.application.usecase.bill.*;
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
@RequestMapping("/api/bills")
@Tag(name = "Contas a Pagar/Receber", description = "Gerenciamento de contas. `type`: pagar | receber")
public class BillController {

    private final CreateBillUseCase create;
    private final UpdateBillUseCase update;
    private final DeleteBillUseCase delete;
    private final ListBillsUseCase list;
    private final MarkBillPaidUseCase markPaid;

    public BillController(CreateBillUseCase create, UpdateBillUseCase update,
                          DeleteBillUseCase delete, ListBillsUseCase list,
                          MarkBillPaidUseCase markPaid) {
        this.create = create;
        this.update = update;
        this.delete = delete;
        this.list = list;
        this.markPaid = markPaid;
    }

    @GetMapping
    @Operation(summary = "Listar contas", description = "Retorna todas as contas ativas (pagas e pendentes). Equivale a `store.bills`.")
    public List<BillResponse> listAll(Authentication auth) {
        return list.execute(uid(auth));
    }

    @PostMapping
    @Operation(
        summary = "Criar conta a pagar/receber",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "Conta a pagar", value = """
                        {
                          "name": "Aluguel",
                          "amount": 1800.00,
                          "dueDate": "2026-06-05",
                          "categoryId": "a1b2c3d4-0000-0000-0000-000000000001",
                          "bankId": "b2c3d4e5-0000-0000-0000-000000000001",
                          "notes": "Vencimento dia 5",
                          "type": "pagar"
                        }
                        """),
                    @ExampleObject(name = "Conta a receber", value = """
                        {
                          "name": "Freelance React",
                          "amount": 3500.00,
                          "dueDate": "2026-06-15",
                          "categoryId": "a1b2c3d4-0000-0000-0000-000000000002",
                          "type": "receber"
                        }
                        """)
                }))
    )
    public ResponseEntity<BillResponse> create(@Valid @RequestBody BillRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(create.execute(uid(auth), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar conta")
    public BillResponse update(
            @Parameter(description = "UUID da conta") @PathVariable UUID id,
            @Valid @RequestBody BillRequest request,
            Authentication auth) {
        return update.execute(uid(auth), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir conta (soft delete)")
    @ApiResponse(responseCode = "204", description = "Excluída com sucesso")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID da conta") @PathVariable UUID id,
            Authentication auth) {
        delete.execute(uid(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/pay")
    @Operation(
        summary = "Marcar como paga",
        description = "Define `paid=true` e `paidDate=hoje`. Equivale ao `markBillPaid` do portal."
    )
    public BillResponse markPaid(
            @Parameter(description = "UUID da conta") @PathVariable UUID id,
            Authentication auth) {
        return markPaid.execute(uid(auth), id);
    }

    private String uid(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getUsername();
        }
        return String.valueOf(principal);
    }
}
