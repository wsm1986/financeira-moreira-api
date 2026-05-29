package com.financeira.api.application.usecase.score;

import com.financeira.api.application.dto.ScoreComponent;
import com.financeira.api.application.dto.ScoreResponse;
import com.financeira.api.domain.model.Category;
import com.financeira.api.domain.model.Entry;
import com.financeira.api.domain.model.Goal;
import com.financeira.api.domain.model.Investment;
import com.financeira.api.domain.repository.CategoryRepository;
import com.financeira.api.domain.repository.EntryRepository;
import com.financeira.api.domain.repository.GoalRepository;
import com.financeira.api.domain.repository.InvestmentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Calcula o Score Financeiro para um mês específico.
 *
 * Espelho exato de useHealthScore() em HealthScore.tsx:
 *
 *   savingsScore  (0–40): (income − debitosReais) / income
 *     >= 30% → 40  |  >= 20% → 32  |  >= 10% → 22  |  >= 0% → 10  |  < 0% → 0
 *
 *   budgetScore   (0–30): inicia em 30, desconta por excesso
 *     por categoria > 100%: −10  |  > 90%: −4  |  > 80%: −2
 *
 *   emergencyScore(0–20): reserva / avgMonthlyExpense
 *     >= 6 meses → 20  |  >= 3 → 14  |  >= 1 → 7  |  else → 0
 *
 *   goalScore     (0–10): metas com status != 'completed'
 *     >= 3 → 10  |  >= 1 → 6  |  else → 0
 */
@Service
public class HealthScoreUseCase {

    /** Kinds que representam saída real de caixa (calcDebitosReais) */
    private static final Set<String> DEBITOS_REAIS_KINDS = Set.of(
            "debito_avista", "debito_recorrente", "pagamento_fatura"
    );

    /** Kinds de gasto para cálculo de orçamento (exclui receita, pagamento_fatura, transferencia) */
    private static final Set<String> SPEND_KINDS = Set.of(
            "debito_avista", "debito_recorrente",
            "credito_avista", "credito_parcelado", "recorrente_cartao"
    );

    /** Kinds de total despesa para calcular "meses de reserva" */
    private static final Set<String> TOTAL_SPEND_KINDS = Set.of(
            "debito_avista", "debito_recorrente",
            "credito_avista", "credito_parcelado", "recorrente_cartao"
            // exclui: receita, pagamento_fatura, transferencia
    );

    private static final String[] SCORE_COLORS = {
            "#f87171",  // Crítico      (< 30)
            "#fb923c",  // Atenção      (30–49)
            "#fbbf24",  // Regular      (50–69)
            "#7c8dff",  // Muito bom    (70–84)
            "#34d399",  // Excelente    (>= 85)
    };
    private static final String[] COMPONENT_COLORS_GOOD = { "#34d399", "#34d399", "#34d399", "#34d399" };

    private final EntryRepository      entryRepo;
    private final CategoryRepository   categoryRepo;
    private final InvestmentRepository investRepo;
    private final GoalRepository       goalRepo;

    public HealthScoreUseCase(EntryRepository entryRepo,
                              CategoryRepository categoryRepo,
                              InvestmentRepository investRepo,
                              GoalRepository goalRepo) {
        this.entryRepo    = entryRepo;
        this.categoryRepo = categoryRepo;
        this.investRepo   = investRepo;
        this.goalRepo     = goalRepo;
    }

    public ScoreResponse execute(String uid, String monthKey) {

        List<Entry>    entries    = entryRepo.findAllByUserUidAndMonthKey(uid, monthKey);
        List<Category> categories = categoryRepo.findAllByUserUid(uid);
        List<Investment> investments = investRepo.findAllByUserUid(uid);
        List<Goal>     goals      = goalRepo.findAllByUserUid(uid);

        // Verificar se há dados
        BigDecimal income = sumByKind(entries, "receita");
        BigDecimal totalSpend = sumByKinds(entries, TOTAL_SPEND_KINDS);

        boolean hasData = !entries.isEmpty() && (
                income.compareTo(BigDecimal.ZERO) > 0 ||
                totalSpend.compareTo(BigDecimal.ZERO) > 0
        );

        if (!hasData) {
            ScoreResponse res = new ScoreResponse();
            res.setMonthKey(monthKey);
            res.setScore(0);
            res.setLabel("Sem dados");
            res.setColor("var(--text3)");
            res.setNoData(true);
            res.setBreakdown(List.of());
            return res;
        }

        // ── 1. Taxa de poupança (0–40 pts) ────────────────────────────────
        // IMPORTANTE: usa calcDebitosReais (saídas reais), NÃO cardSpend — alinhado com HealthScore.tsx
        BigDecimal debitosReais = sumByKinds(entries, DEBITOS_REAIS_KINDS);
        BigDecimal saldo        = income.subtract(debitosReais);
        double savings = income.compareTo(BigDecimal.ZERO) > 0
                ? saldo.divide(income, 6, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        int savingsScore;
        if      (savings >= 0.30) savingsScore = 40;
        else if (savings >= 0.20) savingsScore = 32;
        else if (savings >= 0.10) savingsScore = 22;
        else if (savings >= 0.00) savingsScore = 10;
        else                      savingsScore = 0;

        String savingsDetail = savings >= 0
                ? String.format("%.0f%% da receita", savings * 100)
                : "Mês negativo";
        String savingsColor = savingsScore >= 30 ? "#34d399" : savingsScore >= 20 ? "#fbbf24" : "#f87171";

        // ── 2. Aderência ao orçamento (0–30 pts) ──────────────────────────
        List<Category> catsWithBudget = categories.stream()
                .filter(c -> c.getBudget() != null && c.getBudget().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        int budgetScore = 30;
        if (!catsWithBudget.isEmpty()) {
            // Entradas de gasto (exclui receita, pagamento_fatura, transferencia)
            List<Entry> spendEntries = entries.stream()
                    .filter(e -> SPEND_KINDS.contains(e.getKind()))
                    .toList();

            for (Category cat : catsWithBudget) {
                // Matching por categoryId (backend) — equivalente ao match por nome no frontend
                BigDecimal spent = spendEntries.stream()
                        .filter(e -> cat.getId().equals(e.getCategoryId()))
                        .map(Entry::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                double pct = cat.getBudget().compareTo(BigDecimal.ZERO) > 0
                        ? spent.divide(cat.getBudget(), 6, RoundingMode.HALF_UP).doubleValue()
                        : 0.0;

                if      (pct > 1.00) budgetScore -= 10;
                else if (pct > 0.90) budgetScore -= 4;
                else if (pct > 0.80) budgetScore -= 2;
            }
            budgetScore = Math.max(0, budgetScore);
        }
        String budgetDetail = !catsWithBudget.isEmpty()
                ? catsWithBudget.size() + " categoria" + (catsWithBudget.size() > 1 ? "s" : "")
                : "Sem orçamento";
        String budgetColor = budgetScore >= 22 ? "#34d399" : budgetScore >= 12 ? "#fbbf24" : "#f87171";

        // ── 3. Reserva de emergência (0–20 pts) ───────────────────────────
        // avgExpense = totalSpend atual (selectTotalSpend do mês), ou 1 se zero
        double avgExpense = totalSpend.compareTo(BigDecimal.ZERO) > 0
                ? totalSpend.doubleValue()
                : 1.0;

        double emergencyVal = investments.stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsEmergencyReserve()))
                .mapToDouble(i -> i.getCurrentValue() != null ? i.getCurrentValue().doubleValue() : 0.0)
                .sum();

        double emergencyMonths = emergencyVal / avgExpense;
        int emergencyScore;
        if      (emergencyMonths >= 6) emergencyScore = 20;
        else if (emergencyMonths >= 3) emergencyScore = 14;
        else if (emergencyMonths >= 1) emergencyScore = 7;
        else                           emergencyScore = 0;

        String emergencyDetail = emergencyMonths >= 1
                ? String.format("%.1f meses", emergencyMonths)
                : "Sem reserva";
        String emergencyColor = emergencyScore >= 14 ? "#34d399" : emergencyScore >= 7 ? "#fbbf24" : "#f87171";

        // ── 4. Metas ativas (0–10 pts) ────────────────────────────────────
        long activeGoals = goals.stream()
                .filter(g -> !"completed".equals(g.getStatus()))
                .count();

        int goalScore;
        if      (activeGoals >= 3) goalScore = 10;
        else if (activeGoals >= 1) goalScore = 6;
        else                       goalScore = 0;

        String goalDetail = activeGoals > 0
                ? activeGoals + " ativa" + (activeGoals > 1 ? "s" : "")
                : "Nenhuma meta";
        String goalColor = goalScore >= 8 ? "#34d399" : goalScore >= 4 ? "#fbbf24" : "#f87171";

        // ── Total e label ─────────────────────────────────────────────────
        int total = savingsScore + budgetScore + emergencyScore + goalScore;

        String label;
        if      (total >= 85) label = "Excelente";
        else if (total >= 70) label = "Muito bom";
        else if (total >= 50) label = "Regular";
        else if (total >= 30) label = "Atenção";
        else                  label = "Crítico";

        String color;
        if      (total >= 85) color = "#34d399";
        else if (total >= 70) color = "#7c8dff";
        else if (total >= 50) color = "#fbbf24";
        else if (total >= 30) color = "#fb923c";
        else                  color = "#f87171";

        // ── Breakdown ─────────────────────────────────────────────────────
        List<ScoreComponent> breakdown = List.of(
                new ScoreComponent("Poupança",  savingsScore,  40, savingsDetail,   savingsColor),
                new ScoreComponent("Orçamento", budgetScore,   30, budgetDetail,    budgetColor),
                new ScoreComponent("Reserva",   emergencyScore,20, emergencyDetail, emergencyColor),
                new ScoreComponent("Metas",     goalScore,     10, goalDetail,      goalColor)
        );

        ScoreResponse res = new ScoreResponse();
        res.setMonthKey(monthKey);
        res.setScore(total);
        res.setLabel(label);
        res.setColor(color);
        res.setNoData(false);
        res.setBreakdown(breakdown);
        return res;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private BigDecimal sumByKind(List<Entry> entries, String kind) {
        return entries.stream()
                .filter(e -> kind.equals(e.getKind()))
                .map(Entry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumByKinds(List<Entry> entries, Set<String> kinds) {
        return entries.stream()
                .filter(e -> kinds.contains(e.getKind()))
                .map(Entry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
