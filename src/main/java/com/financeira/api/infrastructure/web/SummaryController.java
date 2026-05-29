package com.financeira.api.infrastructure.web;

import com.financeira.api.application.dto.AnnualSummaryResponse;
import com.financeira.api.application.dto.ScoreResponse;
import com.financeira.api.application.dto.SummaryResponse;
import com.financeira.api.application.usecase.score.HealthScoreUseCase;
import com.financeira.api.application.usecase.summary.AnnualSummaryUseCase;
import com.financeira.api.application.usecase.summary.MonthSummaryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Year;

@RestController
@RequestMapping("/api/summary")
@Tag(name = "Resumo & Score", description = """
    KPIs financeiros, score de saúde e resumo anual — todos calculados no servidor.
    Equivale às fórmulas de DashboardV2.tsx, HealthScore.tsx e Analysis.tsx.
    """)
public class SummaryController {

    private final MonthSummaryUseCase   monthSummaryUseCase;
    private final AnnualSummaryUseCase  annualSummaryUseCase;
    private final HealthScoreUseCase    healthScoreUseCase;

    public SummaryController(MonthSummaryUseCase monthSummaryUseCase,
                             AnnualSummaryUseCase annualSummaryUseCase,
                             HealthScoreUseCase healthScoreUseCase) {
        this.monthSummaryUseCase  = monthSummaryUseCase;
        this.annualSummaryUseCase = annualSummaryUseCase;
        this.healthScoreUseCase   = healthScoreUseCase;
    }

    // ─── GET /api/summary?monthKey=YYYY-MM ────────────────────────────────────

    @GetMapping
    @Operation(
        summary = "KPIs mensais",
        description = """
            Retorna todos os indicadores financeiros do mês informado.

            **Fórmulas (espelho do frontend):**
            - `income`    = SUM(amount) WHERE kind = 'receita'
            - `expense`   = SUM(amount) WHERE kind IN (debito_avista, debito_recorrente, pagamento_fatura)
            - `cardSpend` = SUM(amount) WHERE kind IN (credito_avista, credito_parcelado, recorrente_cartao)

            **Fatura por cartão:**
            - `fatura`   = gastos do cartão no mês anterior (prevMonthKey)
            - `pago`     = pagamento_fatura lançado no mês atual para o mesmo cartão
            - `pendente` = MAX(0, fatura − pago)

            **Saldo:**
            - `saldoAtualizado` = income − expense − totalFaturaPendente
            - `savingsRate`     = income > 0 ? saldoAtualizado / income × 100 : 0

            **Patrimônio:**
            - `netWorth` = totalBancos + totalInvestimentos
            """,
        parameters = @Parameter(
            name = "monthKey",
            description = "Mês no formato YYYY-MM",
            required = true,
            example = "2026-05"
        )
    )
    public SummaryResponse getSummary(
            @RequestParam String monthKey,
            Authentication auth) {
        return monthSummaryUseCase.execute(uid(auth), monthKey);
    }

    // ─── GET /api/summary/annual?year=YYYY ───────────────────────────────────

    @GetMapping("/annual")
    @Operation(
        summary = "Resumo anual (12 meses)",
        description = """
            Retorna os dados mês a mês de Janeiro a Dezembro do ano informado.

            **Meses históricos** (monthKey ≤ mês atual):
            - `receita`  = SUM(amount) WHERE kind='receita'
            - `despesas` = calcDebitosReais (debito_avista + debito_recorrente + pagamento_fatura)
              + totalFaturaPendente se for o mês atual
            - `cardSpend` = total gastos no cartão

            **Meses futuros** (monthKey > mês atual):
            - `despesas` = calcTotalDespesas (todos os kinds de gasto excl. pagamento_fatura e transferencia)
            - `cardSpend` = 0

            Inclui totais anuais, médias mensais e saldo acumulado.
            """,
        parameters = @Parameter(
            name = "year",
            description = "Ano com 4 dígitos",
            required = true,
            example = "2026"
        )
    )
    public AnnualSummaryResponse getAnnualSummary(
            @RequestParam(defaultValue = "0") int year,
            Authentication auth) {
        if (year == 0) year = Year.now().getValue();
        return annualSummaryUseCase.execute(uid(auth), year);
    }

    // ─── GET /api/summary/score?monthKey=YYYY-MM ─────────────────────────────

    @GetMapping("/score")
    @Operation(
        summary = "Score de saúde financeira",
        description = """
            Calcula o score financeiro (0–100) para o mês informado.

            **Componentes:**
            - **Poupança** (0–40): (income − debitosReais) / income
              ≥30% → 40 | ≥20% → 32 | ≥10% → 22 | ≥0% → 10 | negativo → 0
            - **Orçamento** (0–30): inicia 30, desconta por excesso de orçamento
              >100%: −10 | >90%: −4 | >80%: −2 por categoria
            - **Reserva** (0–20): reservaEmergência / despesaAtual em meses
              ≥6 → 20 | ≥3 → 14 | ≥1 → 7 | else → 0
            - **Metas** (0–10): metas ativas (status ≠ completed)
              ≥3 → 10 | ≥1 → 6 | else → 0

            **Labels:** Crítico / Atenção / Regular / Muito bom / Excelente
            """,
        parameters = @Parameter(
            name = "monthKey",
            description = "Mês no formato YYYY-MM",
            required = true,
            example = "2026-05"
        )
    )
    public ScoreResponse getScore(
            @RequestParam String monthKey,
            Authentication auth) {
        return healthScoreUseCase.execute(uid(auth), monthKey);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private String uid(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getUsername();
        }
        return String.valueOf(principal);
    }
}
