package com.financeira.api.application.usecase.summary;

import com.financeira.api.application.dto.AnnualMonthRow;
import com.financeira.api.application.dto.AnnualSummaryResponse;
import com.financeira.api.domain.model.Entry;
import com.financeira.api.domain.repository.EntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calcula o resumo anual com breakdown mês a mês.
 *
 * Espelho de ytdData (useMemo) em Analysis.tsx:
 *
 *   Para meses históricos (monthKey <= currentMonthKey):
 *     receita   = SUM WHERE kind='receita'
 *     despesas  = calcDebitosReais = SUM WHERE kind IN (debito_avista, debito_recorrente, pagamento_fatura)
 *                 + totalPendenteFatura se monthKey == currentMonthKey
 *     cardSpend = SUM WHERE kind IN (credito_avista, credito_parcelado, recorrente_cartao)
 *     saldo     = receita − despesas
 *
 *   Para meses futuros (monthKey > currentMonthKey):
 *     receita   = SUM WHERE kind='receita'
 *     despesas  = calcTotalDespesas = SUM WHERE kind IN (todos excl. receita, pagamento_fatura, transferencia)
 *     cardSpend = 0
 *     saldo     = receita − despesas
 *
 *   saldoAcumulado = soma cumulativa mês a mês (apenas meses com dados)
 *
 *   Totais anuais: soma dos meses históricos com dados
 *   avgMonthly: totais / monthsWithData
 */
@Service
public class AnnualSummaryUseCase {

    private static final Set<String> DEBITOS_REAIS_KINDS = Set.of(
            "debito_avista", "debito_recorrente", "pagamento_fatura"
    );
    private static final Set<String> CARD_KINDS = Set.of(
            "credito_avista", "credito_parcelado", "recorrente_cartao"
    );
    /** calcTotalDespesas — para meses futuros */
    private static final Set<String> TOTAL_DESPESAS_KINDS = Set.of(
            "debito_avista", "debito_recorrente",
            "credito_avista", "credito_parcelado", "recorrente_cartao"
    );

    private static final String[] MONTH_LABELS = {
            "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
            "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    };

    private final EntryRepository entryRepo;
    private final MonthSummaryUseCase monthSummaryUseCase;

    public AnnualSummaryUseCase(EntryRepository entryRepo,
                                MonthSummaryUseCase monthSummaryUseCase) {
        this.entryRepo           = entryRepo;
        this.monthSummaryUseCase = monthSummaryUseCase;
    }

    public AnnualSummaryResponse execute(String uid, int year) {

        // Mês atual (para distinguir histórico vs futuro)
        YearMonth currentYm    = YearMonth.now();
        String    currentMk    = currentYm.toString(); // "2026-05"

        // Fatura pendente do mês atual (para ajuste de despesas no mês corrente)
        BigDecimal totalPendenteFatura = BigDecimal.ZERO;
        if (currentYm.getYear() == year) {
            // Reutilizar MonthSummaryUseCase para obter totalFaturaPendente
            var summary = monthSummaryUseCase.execute(uid, currentMk);
            totalPendenteFatura = summary.getTotalFaturaPendente();
        }

        // Carregar todos os lançamentos do ano de uma só vez (evita N queries)
        // Usando findAllByUserUidAndMonthKey para cada mês
        // (ou poderíamos ter findAllByUserUidAndYear — mas não existe ainda, então por mês)
        final BigDecimal pendenteFinal = totalPendenteFatura;

        List<AnnualMonthRow> monthRows = new ArrayList<>();
        BigDecimal runningBalance = BigDecimal.ZERO;
        BigDecimal totalReceitaAcc  = BigDecimal.ZERO;
        BigDecimal totalDespesasAcc = BigDecimal.ZERO;
        int        monthsWithDataAcc = 0;

        for (int m = 1; m <= 12; m++) {
            String mk = String.format("%d-%02d", year, m);
            List<Entry> mEntries = entryRepo.findAllByUserUidAndMonthKey(uid, mk);

            boolean isFuture = mk.compareTo(currentMk) > 0;
            boolean hasData  = !mEntries.isEmpty();

            BigDecimal receita   = sum(mEntries, "receita");
            BigDecimal despesas;
            BigDecimal cardSpend;

            if (!isFuture) {
                // Mês histórico: calcDebitosReais
                despesas  = sumKinds(mEntries, DEBITOS_REAIS_KINDS);
                cardSpend = sumKinds(mEntries, CARD_KINDS);

                // Mês corrente: somar fatura pendente do mês anterior
                if (mk.equals(currentMk)) {
                    despesas = despesas.add(pendenteFinal);
                }
            } else {
                // Mês futuro: calcTotalDespesas (compromissos já lançados)
                despesas  = sumKinds(mEntries, TOTAL_DESPESAS_KINDS);
                cardSpend = BigDecimal.ZERO;
            }

            BigDecimal saldo = receita.subtract(despesas);

            // Saldo acumulado: acumula apenas meses com dados
            if (hasData) {
                runningBalance = runningBalance.add(saldo);
            }

            // Totais anuais: apenas meses históricos com dados
            if (!isFuture && hasData) {
                totalReceitaAcc  = totalReceitaAcc.add(receita);
                totalDespesasAcc = totalDespesasAcc.add(despesas);
                monthsWithDataAcc++;
            }

            AnnualMonthRow row = new AnnualMonthRow();
            row.setMonthKey(mk);
            row.setLabel(MONTH_LABELS[m - 1]);
            row.setReceita(receita);
            row.setDespesas(despesas);
            row.setSaldo(saldo);
            row.setCardSpend(cardSpend);
            row.setSaldoAcumulado(hasData ? runningBalance : null);
            row.setIsFuture(isFuture);
            row.setHasData(hasData);

            monthRows.add(row);
        }

        BigDecimal saldoAnual = totalReceitaAcc.subtract(totalDespesasAcc);
        double savingsRate = totalReceitaAcc.compareTo(BigDecimal.ZERO) > 0
                ? saldoAnual.divide(totalReceitaAcc, 6, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        int denom = Math.max(1, monthsWithDataAcc);
        BigDecimal avgReceita  = totalReceitaAcc.divide(BigDecimal.valueOf(denom), 2, RoundingMode.HALF_UP);
        BigDecimal avgDespesas = totalDespesasAcc.divide(BigDecimal.valueOf(denom), 2, RoundingMode.HALF_UP);

        AnnualSummaryResponse res = new AnnualSummaryResponse();
        res.setYear(year);
        res.setMonths(monthRows);
        res.setTotalReceita(totalReceitaAcc);
        res.setTotalDespesas(totalDespesasAcc);
        res.setSaldo(saldoAnual);
        res.setSavingsRate(savingsRate);
        res.setAvgMonthlyReceita(avgReceita);
        res.setAvgMonthlyDespesas(avgDespesas);
        res.setMonthsWithData(monthsWithDataAcc);
        return res;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private BigDecimal sum(List<Entry> entries, String kind) {
        return entries.stream()
                .filter(e -> kind.equals(e.getKind()))
                .map(Entry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumKinds(List<Entry> entries, Set<String> kinds) {
        return entries.stream()
                .filter(e -> kinds.contains(e.getKind()))
                .map(Entry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
