package com.financeira.api.application.usecase.summary;

import com.financeira.api.application.dto.CardInvoiceSummary;
import com.financeira.api.application.dto.CategorySpendSummary;
import com.financeira.api.application.dto.SummaryResponse;
import com.financeira.api.domain.model.Bank;
import com.financeira.api.domain.model.Category;
import com.financeira.api.domain.model.CreditCard;
import com.financeira.api.domain.model.Entry;
import com.financeira.api.domain.model.Investment;
import com.financeira.api.domain.repository.BankRepository;
import com.financeira.api.domain.repository.CategoryRepository;
import com.financeira.api.domain.repository.CreditCardRepository;
import com.financeira.api.domain.repository.EntryRepository;
import com.financeira.api.domain.repository.InvestmentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Calcula todos os KPIs financeiros do mês para um usuário.
 *
 * Fórmulas espelho do frontend (DashboardV2.tsx):
 *
 *   INCOME    = SUM(amount) WHERE kind = 'receita'
 *   EXPENSE   = SUM(amount) WHERE kind IN (debito_avista, debito_recorrente, pagamento_fatura)
 *   CARD_SPEND= SUM(amount) WHERE kind IN (credito_avista, credito_parcelado, recorrente_cartao)
 *
 *   FATURA por cartão (mês anterior):
 *     fatura   = SUM(amount) de card_kinds com monthKey = prevMonthKey e cardId = card.id
 *     pago     = SUM(amount) de pagamento_fatura com monthKey = mk e cardId = card.id
 *     pendente = MAX(0, fatura - pago)
 *
 *   SALDO_ATUALIZADO = income - expense - totalPendenteFatura
 *   SAVINGS_RATE     = income > 0 ? saldoAtualizado / income * 100 : 0
 *   NET_WORTH        = totalBancos + totalInvestimentos
 */
@Service
public class MonthSummaryUseCase {

    private static final Set<String> CARD_KINDS = Set.of(
            "credito_avista", "credito_parcelado", "recorrente_cartao"
    );
    private static final Set<String> EXPENSE_KINDS = Set.of(
            "debito_avista", "debito_recorrente", "pagamento_fatura"
    );
    private static final Set<String> SPEND_KINDS_FOR_CATEGORY = Set.of(
            "debito_avista", "debito_recorrente",
            "credito_avista", "credito_parcelado", "recorrente_cartao"
            // exclui: receita, pagamento_fatura, transferencia
    );

    private final EntryRepository      entryRepo;
    private final BankRepository       bankRepo;
    private final CreditCardRepository cardRepo;
    private final InvestmentRepository investRepo;
    private final CategoryRepository   categoryRepo;

    public MonthSummaryUseCase(EntryRepository entryRepo,
                               BankRepository bankRepo,
                               CreditCardRepository cardRepo,
                               InvestmentRepository investRepo,
                               CategoryRepository categoryRepo) {
        this.entryRepo    = entryRepo;
        this.bankRepo     = bankRepo;
        this.cardRepo     = cardRepo;
        this.investRepo   = investRepo;
        this.categoryRepo = categoryRepo;
    }

    public SummaryResponse execute(String uid, String monthKey) {

        // ── Mês anterior (para fatura) ────────────────────────────────────────
        YearMonth ym           = YearMonth.parse(monthKey);
        YearMonth ymPrev       = ym.minusMonths(1);
        YearMonth ymNext       = ym.plusMonths(1);
        String    prevMonthKey = ymPrev.toString();   // ex: "2026-05"
        String    nextMonthKey = ymNext.toString();   // ex: "2026-07"

        // ── Carregar lançamentos ──────────────────────────────────────────────
        List<Entry> currentEntries = entryRepo.findAllByUserUidAndMonthKey(uid, monthKey);
        List<Entry> prevEntries    = entryRepo.findAllByUserUidAndMonthKey(uid, prevMonthKey);

        // ── KPIs básicos (mês atual) ──────────────────────────────────────────
        BigDecimal income    = sumByKinds(currentEntries, Set.of("receita"));
        BigDecimal expense   = sumByKinds(currentEntries, EXPENSE_KINDS);
        BigDecimal cardSpend = sumByKinds(currentEntries, CARD_KINDS);

        // ── Fatura por cartão ─────────────────────────────────────────────────
        List<CreditCard> cards = cardRepo.findAllByUserUid(uid);

        // nextEntries carregado apenas se há cartões: pagamento_fatura pode ter monthKey=nextMonth(mk)
        List<Entry> nextEntries = cards.isEmpty()
                ? List.of()
                : entryRepo.findAllByUserUidAndMonthKey(uid, nextMonthKey);

        List<CardInvoiceSummary> faturasPorCartao = cards.stream().map(card -> {
            // Compras do mês anterior nesse cartão
            BigDecimal fatura = prevEntries.stream()
                    .filter(e -> card.getId().equals(e.getCardId())
                              && CARD_KINDS.contains(e.getKind()))
                    .map(Entry::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Pagamentos de fatura para esse cartão: busca em mk E nextMonth(mk).
            // Pagamento via Dashboard usa monthKey=mk; via CardsPage pode usar monthKey=nextMonth(mk).
            BigDecimal pago = Stream.concat(currentEntries.stream(), nextEntries.stream())
                    .filter(e -> card.getId().equals(e.getCardId())
                              && "pagamento_fatura".equals(e.getKind()))
                    .map(Entry::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal pendente = fatura.subtract(pago).max(BigDecimal.ZERO);

            return new CardInvoiceSummary(card.getId(), card.getName(), fatura, pago, pendente);
        }).collect(Collectors.toList());

        BigDecimal totalFatura        = faturasPorCartao.stream()
                .map(CardInvoiceSummary::getFatura).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFaturaPaga    = faturasPorCartao.stream()
                .map(CardInvoiceSummary::getPago).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFaturaPendente = faturasPorCartao.stream()
                .map(CardInvoiceSummary::getPendente).reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── Patrimônio ────────────────────────────────────────────────────────
        List<Bank>       banks    = bankRepo.findAllByUserUid(uid);
        List<Investment> invests  = investRepo.findAllByUserUid(uid);

        BigDecimal totalBancos = banks.stream()
                .map(b -> b.getBalance() != null ? b.getBalance() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInvestimentos = invests.stream()
                .map(i -> i.getCurrentValue() != null ? i.getCurrentValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netWorth = totalBancos.add(totalInvestimentos);

        // ── Saldos calculados ─────────────────────────────────────────────────
        BigDecimal saldoAtualizado = income.subtract(expense).subtract(totalFaturaPendente);
        // saldoProjetado = saldo real dos bancos − fatura pendente (fluxo de caixa real).
        // Fórmula: totalBancos - totalFaturaPendente (espelho do fallback frontend)
        BigDecimal saldoProjetado  = totalBancos.subtract(totalFaturaPendente);

        double savingsRate = income.compareTo(BigDecimal.ZERO) > 0
                ? saldoAtualizado.divide(income, 6, RoundingMode.HALF_UP)
                              .multiply(BigDecimal.valueOf(100))
                              .doubleValue()
                : 0.0;

        // ── Breakdown por categoria ───────────────────────────────────────────
        List<Category> categories = categoryRepo.findAllByUserUid(uid);
        Map<UUID, Category> catById = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        // Agrupa gastos por categoryId no mês atual (apenas kinds de gasto)
        Map<UUID, BigDecimal> spendByCat = new HashMap<>();
        for (Entry e : currentEntries) {
            if (SPEND_KINDS_FOR_CATEGORY.contains(e.getKind()) && e.getCategoryId() != null) {
                spendByCat.merge(e.getCategoryId(), e.getAmount(), BigDecimal::add);
            }
        }

        List<CategorySpendSummary> categoryBreakdown = categories.stream().map(cat -> {
            BigDecimal gasto  = spendByCat.getOrDefault(cat.getId(), BigDecimal.ZERO);
            BigDecimal budget = cat.getBudget() != null ? cat.getBudget() : BigDecimal.ZERO;
            double pct = budget.compareTo(BigDecimal.ZERO) > 0
                    ? gasto.divide(budget, 6, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;
            return new CategorySpendSummary(cat.getId(), cat.getName(), budget, gasto, pct);
        }).collect(Collectors.toList());

        // ── Montar resposta ───────────────────────────────────────────────────
        SummaryResponse res = new SummaryResponse();
        res.setMonthKey(monthKey);
        res.setIncome(income);
        res.setExpense(expense);
        res.setCardSpend(cardSpend);
        res.setPrevMonthKey(prevMonthKey);
        res.setTotalFatura(totalFatura);
        res.setTotalFaturaPaga(totalFaturaPaga);
        res.setTotalFaturaPendente(totalFaturaPendente);
        res.setFaturasPorCartao(faturasPorCartao);
        res.setSaldoAtualizado(saldoAtualizado);
        res.setSaldoProjetado(saldoProjetado);
        res.setSavingsRate(savingsRate);
        res.setTotalBancos(totalBancos);
        res.setTotalInvestimentos(totalInvestimentos);
        res.setNetWorth(netWorth);
        res.setCategoryBreakdown(categoryBreakdown);
        return res;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private BigDecimal sumByKinds(List<Entry> entries, Set<String> kinds) {
        return entries.stream()
                .filter(e -> kinds.contains(e.getKind()))
                .map(Entry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
