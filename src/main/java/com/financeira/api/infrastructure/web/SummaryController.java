package com.financeira.api.infrastructure.web;

import com.financeira.api.application.dto.SummaryResponse;
import com.financeira.api.application.usecase.summary.MonthSummaryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/summary")
@Tag(name = "Resumo Mensal", description = """
    KPIs financeiros calculados no servidor para um dado mês.
    Equivale a todas as fórmulas de DashboardV2.tsx — income, expense, cardSpend,
    fatura por cartão, saldoAtualizado, savingsRate, netWorth e breakdown de categorias.
    """)
public class SummaryController {

    private final MonthSummaryUseCase summaryUseCase;

    public SummaryController(MonthSummaryUseCase summaryUseCase) {
        this.summaryUseCase = summaryUseCase;
    }

    @GetMapping
    @Operation(
        summary = "KPIs do mês",
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
        return summaryUseCase.execute(uid(auth), monthKey);
    }

    private String uid(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getUsername();
        }
        return String.valueOf(principal);
    }
}
