package com.financeira.api.scheduler;

import com.financeira.api.application.dto.CardInvoiceSummary;
import com.financeira.api.application.dto.SummaryResponse;
import com.financeira.api.application.usecase.summary.MonthSummaryUseCase;
import com.financeira.api.domain.model.Bill;
import com.financeira.api.domain.repository.BillRepository;
import com.financeira.api.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Envia um resumo financeiro diário via WhatsApp às 8h (horário de Brasília).
 *
 * Configuração necessária no Render (env vars):
 *   CALLMEBOT_PHONE   — já existia
 *   CALLMEBOT_APIKEY  — já existia
 *   NOTIFICACAO_USER_UID — UID do Firebase do usuário que receberá o resumo
 *
 * O scheduler lê o mês atual, monta a mensagem e envia pelo WhatsAppService existente.
 * Bills com vencimento nos próximos 3 dias aparecem em destaque no topo da mensagem.
 */
@Component
public class ResumoFinanceiroScheduler {

    private static final Logger log = LoggerFactory.getLogger(ResumoFinanceiroScheduler.class);

    private static final int DIAS_ALERTA_BILL = 3;
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM");
    private static final Locale PT_BR = new Locale("pt", "BR");

    private final MonthSummaryUseCase monthSummaryUseCase;
    private final BillRepository      billRepository;
    private final WhatsAppService     whatsAppService;

    @Value("${notificacao.user-uid:}")
    private String userUid;

    public ResumoFinanceiroScheduler(MonthSummaryUseCase monthSummaryUseCase,
                                     BillRepository billRepository,
                                     WhatsAppService whatsAppService) {
        this.monthSummaryUseCase = monthSummaryUseCase;
        this.billRepository      = billRepository;
        this.whatsAppService     = whatsAppService;
    }

    /**
     * Executa todo dia às 08:00 horário de Brasília (America/Sao_Paulo).
     * Para alterar o horário, mude o cron: "0 0 8 * * *" = segundo, minuto, hora, dia, mês, diasemana
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "America/Sao_Paulo")
    public void enviarResumoDiario() {
        if (userUid == null || userUid.isBlank()) {
            log.warn("[ResumoFinanceiro] NOTIFICACAO_USER_UID nao configurado — scheduler ignorado");
            return;
        }

        log.info("[ResumoFinanceiro] Gerando resumo para uid={}", userUid);

        try {
            String monthKey = YearMonth.now().toString(); // ex: "2026-06"
            SummaryResponse summary = monthSummaryUseCase.execute(userUid, monthKey);
            List<Bill> billsAlerta  = buscarBillsProximasDoVencimento();

            String mensagem = montarMensagem(summary, billsAlerta, monthKey);
            String status   = whatsAppService.enviar(mensagem);

            if ("ok".equals(status)) {
                log.info("[ResumoFinanceiro] Enviado com sucesso");
            } else {
                log.warn("[ResumoFinanceiro] Falha ao enviar: {}", status);
            }

        } catch (Exception e) {
            log.error("[ResumoFinanceiro] Erro ao gerar resumo", e);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private List<Bill> buscarBillsProximasDoVencimento() {
        LocalDate hoje  = LocalDate.now();
        LocalDate limite = hoje.plusDays(DIAS_ALERTA_BILL);

        return billRepository.findAllByUserUid(userUid).stream()
                .filter(b -> !Boolean.TRUE.equals(b.getPaid()))
                .filter(b -> b.getDueDate() != null
                          && !b.getDueDate().isBefore(hoje)
                          && !b.getDueDate().isAfter(limite))
                .sorted((a, b) -> a.getDueDate().compareTo(b.getDueDate()))
                .toList();
    }

    private String montarMensagem(SummaryResponse s, List<Bill> billsAlerta, String monthKey) {
        StringBuilder sb = new StringBuilder();

        // ── Cabeçalho ──────────────────────────────────────────────────────────
        String mesLabel = mesLabel(monthKey);
        sb.append("*💰 Financeira Moreira — ").append(mesLabel).append("*\n");
        sb.append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");

        // ── Alertas de contas a vencer (destaque) ──────────────────────────────
        if (!billsAlerta.isEmpty()) {
            sb.append("\n*⚠️ CONTAS A VENCER EM BREVE:*\n");
            for (Bill bill : billsAlerta) {
                long diasRestantes = LocalDate.now().until(bill.getDueDate()).getDays();
                String quando = diasRestantes == 0 ? "HOJE"
                             : diasRestantes == 1 ? "amanhã"
                             : "em " + diasRestantes + " dias";
                sb.append("  🔴 *").append(bill.getName()).append("*")
                  .append(" — ").append(fmt(bill.getAmount()))
                  .append(" (vence ").append(quando)
                  .append(" — ").append(bill.getDueDate().format(FMT_DATA)).append(")\n");
            }
        }

        // ── KPIs do mês ────────────────────────────────────────────────────────
        sb.append("\n*📊 RESUMO DO MÊS:*\n");
        sb.append("  Receitas:        ").append(fmt(s.getIncome())).append("\n");
        sb.append("  Saídas do banco: ").append(fmt(s.getExpense())).append("\n");
        sb.append("  Cartão (crédito):").append(fmt(s.getCardSpend())).append("\n");

        // ── Saldo atualizado ───────────────────────────────────────────────────
        BigDecimal saldo = s.getSaldoAtualizado() != null ? s.getSaldoAtualizado() : BigDecimal.ZERO;
        String saldoIcon = saldo.compareTo(BigDecimal.ZERO) >= 0 ? "🟢" : "🔴";
        sb.append("\n*").append(saldoIcon).append(" Saldo Atualizado: ").append(fmt(saldo)).append("*\n");

        // ── Fatura pendente por cartão ─────────────────────────────────────────
        if (s.getFaturasPorCartao() != null && !s.getFaturasPorCartao().isEmpty()) {
            List<CardInvoiceSummary> pendentes = s.getFaturasPorCartao().stream()
                    .filter(c -> c.getPendente() != null
                              && c.getPendente().compareTo(BigDecimal.ZERO) > 0)
                    .toList();

            if (!pendentes.isEmpty()) {
                sb.append("\n*💳 FATURAS PENDENTES:*\n");
                for (CardInvoiceSummary c : pendentes) {
                    sb.append("  • ").append(c.getCardName())
                      .append(": ").append(fmt(c.getPendente())).append("\n");
                }
                sb.append("  *Total: ").append(fmt(s.getTotalFaturaPendente())).append("*\n");
            }
        }

        // ── Patrimônio ─────────────────────────────────────────────────────────
        if (s.getNetWorth() != null && s.getNetWorth().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("\n*🏦 PATRIMÔNIO:*\n");
            sb.append("  Bancos:       ").append(fmt(s.getTotalBancos())).append("\n");
            if (s.getTotalInvestimentos() != null
                    && s.getTotalInvestimentos().compareTo(BigDecimal.ZERO) > 0) {
                sb.append("  Investimentos:").append(fmt(s.getTotalInvestimentos())).append("\n");
            }
            sb.append("  *Total:       ").append(fmt(s.getNetWorth())).append("*\n");
        }

        // ── Taxa de poupança ───────────────────────────────────────────────────
        if (s.getIncome() != null && s.getIncome().compareTo(BigDecimal.ZERO) > 0) {
            String poupancaIcon = s.getSavingsRate() >= 20 ? "✅"
                               : s.getSavingsRate() >= 0  ? "⚠️" : "❌";
            sb.append("\n").append(poupancaIcon)
              .append(" Poupança: ").append(String.format(PT_BR, "%.1f%%", s.getSavingsRate()))
              .append("\n");
        }

        sb.append("\n_Financeira Moreira_");
        return sb.toString();
    }

    private String fmt(BigDecimal value) {
        if (value == null) return "R$ 0,00";
        return String.format(PT_BR, "R$ %,.2f", value);
    }

    private String mesLabel(String monthKey) {
        // "2026-06" → "Jun/2026"
        YearMonth ym = YearMonth.parse(monthKey);
        return ym.format(DateTimeFormatter.ofPattern("MMM/yyyy", PT_BR))
                  .replace(".", "")
                  .substring(0, 1).toUpperCase()
               + ym.format(DateTimeFormatter.ofPattern("MMM/yyyy", PT_BR))
                  .replace(".", "").substring(1);
    }
}
