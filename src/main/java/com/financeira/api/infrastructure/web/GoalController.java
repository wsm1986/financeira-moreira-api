package com.financeira.api.infrastructure.web;

import com.financeira.api.application.dto.GoalRequest;
import com.financeira.api.application.dto.GoalResponse;
import com.financeira.api.application.usecase.goal.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/goals")
@Tag(name = "Metas Financeiras", description = "Metas de poupança. `status`: on-track | at-risk | great | completed. `deadline` no formato YYYY-MM.")
public class GoalController {

    private final CreateGoalUseCase create;
    private final UpdateGoalUseCase update;
    private final DeleteGoalUseCase delete;
    private final ListGoalsUseCase list;
    private final AddGoalProgressUseCase addProgress;

    public GoalController(CreateGoalUseCase create, UpdateGoalUseCase update,
                          DeleteGoalUseCase delete, ListGoalsUseCase list,
                          AddGoalProgressUseCase addProgress) {
        this.create = create;
        this.update = update;
        this.delete = delete;
        this.list = list;
        this.addProgress = addProgress;
    }

    @GetMapping
    @Operation(summary = "Listar metas", description = "Equivale a `store.goals`.")
    public List<GoalResponse> listAll(Authentication auth) {
        return list.execute(uid(auth));
    }

    @PostMapping
    @Operation(
        summary = "Criar meta",
        description = "`color` aceita hex (`#fbbf24`) ou CSS variable (`var(--amber)`) do portal.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Meta criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
        }
    )
    public ResponseEntity<GoalResponse> create(@Valid @RequestBody GoalRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(create.execute(uid(auth), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar meta")
    public GoalResponse update(
            @Parameter(description = "UUID da meta") @PathVariable UUID id,
            @Valid @RequestBody GoalRequest request,
            Authentication auth) {
        return update.execute(uid(auth), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir meta (soft delete)")
    @ApiResponse(responseCode = "204", description = "Excluída com sucesso")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID da meta") @PathVariable UUID id,
            Authentication auth) {
        delete.execute(uid(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/progress")
    @Operation(summary = "Adicionar progresso", description = "Soma `amount` ao `currentAmount` da meta. Equivale ao `addGoalProgress` do portal.")
    public GoalResponse addProgress(
            @Parameter(description = "UUID da meta") @PathVariable UUID id,
            @RequestBody Map<String, BigDecimal> body,
            Authentication auth) {
        return addProgress.execute(uid(auth), id, body.get("amount"));
    }

    private String uid(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getUsername();
        }
        return String.valueOf(principal);
    }
}
