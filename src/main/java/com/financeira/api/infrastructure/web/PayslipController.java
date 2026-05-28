package com.financeira.api.infrastructure.web;

import com.financeira.api.application.dto.PayslipRequest;
import com.financeira.api.application.dto.PayslipResponse;
import com.financeira.api.application.usecase.payslip.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payslips")
@Tag(name = "Holerites", description = "Contracheques mensais. Único por competência (YYYY-MM) por usuário — POST atualiza se já existir.")
public class PayslipController {

    private final SavePayslipUseCase save;
    private final DeletePayslipUseCase delete;
    private final ListPayslipsUseCase list;

    public PayslipController(SavePayslipUseCase save, DeletePayslipUseCase delete, ListPayslipsUseCase list) {
        this.save = save;
        this.delete = delete;
        this.list = list;
    }

    @GetMapping
    @Operation(summary = "Listar holerites", description = "Retorna todos os holerites em ordem cronológica. Equivale a `store.payslips`.")
    public List<PayslipResponse> listAll(Authentication auth) {
        return list.execute(uid(auth));
    }

    @PostMapping
    @Operation(
        summary = "Salvar holerite (criar ou atualizar)",
        description = """
            Se já existir holerite para a `competencia`, **atualiza**. Caso contrário, **cria**.
            - `extras`: proventos variáveis (PLR, horas extras, gratificações)
            - `outrosDescontos`: descontos livres não mapeados nos campos fixos
            - Todos os campos numéricos em R$ — omitir ou 0.00 para não aplicar
            """
    )
    public PayslipResponse save(@Valid @RequestBody PayslipRequest request, Authentication auth) {
        return save.execute(uid(auth), request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir holerite (soft delete)")
    @ApiResponse(responseCode = "204", description = "Excluído com sucesso")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID do holerite") @PathVariable UUID id,
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
