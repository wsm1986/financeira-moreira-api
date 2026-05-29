package com.financeira.api.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resposta completa de GET /api/summary?monthKey=YYYY-MM
 *
 * Fórmulas (espelho exato do frontend — DashboardV2.tsx):
 *
 *   income    = SUM(amount) WHERE kind='receita'
 *   expense   = SUM(amount) WHERE kind IN (debito_avista, debito_recorrente, pagamento_fatura)
 *   cardSpend = SUM(amount) WHERE kind IN (credito_avista, credito_parcelado, recorrente_cartao)
 *
 *   fatura por cartão:
 *     fatura   = SUM(amount) de card_kinds no mês anterior (billingMonth = prevMonthKey)
 *     pago     = SUM(amount) de pagamento_fatura no mês atual para esse cardId
 *     pendente = MAX(0, fatura - pago)
 *
 *   totalPendenteFatura = SUM(pendente) de todos os cartões
 *   saldoAtualizado     = income - expense - totalPendenteFatura
 *   saldoProjetado      = saldoAtualizado   (mesma fórmula por ora)
 *   savingsRate         = income > 0 ? saldoAtualizado / income * 100 : 0
 *
 *   totalBancos          = SUM(bank.balance)
 *   totalInvestimentos   = SUM(investment.currentValue)
 *   netWorth             = totalBancos + totalInvestimentos
 */
public class SummaryResponse {

    private String monthKey;

    // ── KPIs do mês ──────────────────────────────────────────────────────────
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal cardSpend;

    // ── Fatura ───────────────────────────────────────────────────────────────
    /** Mês de referência da fatura (monthKey - 1) */
    private String     prevMonthKey;
    private BigDecimal totalFatura;
    private BigDecimal totalFaturaPaga;
    private BigDecimal totalFaturaPendente;
    private List<CardInvoiceSummary> faturasPorCartao;

    // ── Saldos calculados ────────────────────────────────────────────────────
    private BigDecimal saldoAtualizado;
    private BigDecimal saldoProjetado;
    /** % de poupança — pode ser negativa */
    private double     savingsRate;

    // ── Patrimônio ───────────────────────────────────────────────────────────
    private BigDecimal totalBancos;
    private BigDecimal totalInvestimentos;
    private BigDecimal netWorth;

    // ── Breakdown por categoria ──────────────────────────────────────────────
    private List<CategorySpendSummary> categoryBreakdown;

    public SummaryResponse() {}

    // ── getters / setters ────────────────────────────────────────────────────

    public String     getMonthKey()             { return monthKey; }
    public BigDecimal getIncome()               { return income; }
    public BigDecimal getExpense()              { return expense; }
    public BigDecimal getCardSpend()            { return cardSpend; }
    public String     getPrevMonthKey()         { return prevMonthKey; }
    public BigDecimal getTotalFatura()          { return totalFatura; }
    public BigDecimal getTotalFaturaPaga()      { return totalFaturaPaga; }
    public BigDecimal getTotalFaturaPendente()  { return totalFaturaPendente; }
    public List<CardInvoiceSummary> getFaturasPorCartao() { return faturasPorCartao; }
    public BigDecimal getSaldoAtualizado()      { return saldoAtualizado; }
    public BigDecimal getSaldoProjetado()       { return saldoProjetado; }
    public double     getSavingsRate()          { return savingsRate; }
    public BigDecimal getTotalBancos()          { return totalBancos; }
    public BigDecimal getTotalInvestimentos()   { return totalInvestimentos; }
    public BigDecimal getNetWorth()             { return netWorth; }
    public List<CategorySpendSummary> getCategoryBreakdown() { return categoryBreakdown; }

    public void setMonthKey(String v)                          { this.monthKey = v; }
    public void setIncome(BigDecimal v)                        { this.income = v; }
    public void setExpense(BigDecimal v)                       { this.expense = v; }
    public void setCardSpend(BigDecimal v)                     { this.cardSpend = v; }
    public void setPrevMonthKey(String v)                      { this.prevMonthKey = v; }
    public void setTotalFatura(BigDecimal v)                   { this.totalFatura = v; }
    public void setTotalFaturaPaga(BigDecimal v)               { this.totalFaturaPaga = v; }
    public void setTotalFaturaPendente(BigDecimal v)           { this.totalFaturaPendente = v; }
    public void setFaturasPorCartao(List<CardInvoiceSummary> v){ this.faturasPorCartao = v; }
    public void setSaldoAtualizado(BigDecimal v)               { this.saldoAtualizado = v; }
    public void setSaldoProjetado(BigDecimal v)                { this.saldoProjetado = v; }
    public void setSavingsRate(double v)                       { this.savingsRate = v; }
    public void setTotalBancos(BigDecimal v)                   { this.totalBancos = v; }
    public void setTotalInvestimentos(BigDecimal v)            { this.totalInvestimentos = v; }
    public void setNetWorth(BigDecimal v)                      { this.netWorth = v; }
    public void setCategoryBreakdown(List<CategorySpendSummary> v) { this.categoryBreakdown = v; }
}
