package com.financeira.api.application.dto;

import java.math.BigDecimal;

/**
 * Linha de dados mensais dentro de AnnualSummaryResponse.
 * Espelho do monthRows[] em Analysis.tsx (ytdData).
 */
public class AnnualMonthRow {

    /** Ex: "2026-05" */
    private String     monthKey;
    /** Ex: "Mai" */
    private String     label;
    /** SUM(amount) WHERE kind='receita' */
    private BigDecimal receita;
    /**
     * Meses históricos (monthKey <= currentMonthKey):
     *   calcDebitosReais = debito_avista + debito_recorrente + pagamento_fatura
     *   + totalFaturaPendente se monthKey == currentMonthKey
     * Meses futuros (monthKey > currentMonthKey):
     *   calcTotalDespesas = todos os kinds de despesa excl. pagamento_fatura e transferencia
     */
    private BigDecimal despesas;
    /** receita − despesas */
    private BigDecimal saldo;
    /** SUM(amount) WHERE kind IN (credito_avista, credito_parcelado, recorrente_cartao) */
    private BigDecimal cardSpend;
    /** Saldo acumulado (running total) dentro do ano */
    private BigDecimal saldoAcumulado;
    /** true se o mês ainda não ocorreu */
    private boolean    isFuture;
    /** true se há pelo menos uma entry neste mês */
    private boolean    hasData;

    public AnnualMonthRow() {}

    public String     getMonthKey()       { return monthKey; }
    public String     getLabel()          { return label; }
    public BigDecimal getReceita()        { return receita; }
    public BigDecimal getDespesas()       { return despesas; }
    public BigDecimal getSaldo()          { return saldo; }
    public BigDecimal getCardSpend()      { return cardSpend; }
    public BigDecimal getSaldoAcumulado() { return saldoAcumulado; }
    public boolean    isIsFuture()        { return isFuture; }
    public boolean    isHasData()         { return hasData; }

    public void setMonthKey(String v)           { this.monthKey       = v; }
    public void setLabel(String v)              { this.label          = v; }
    public void setReceita(BigDecimal v)        { this.receita        = v; }
    public void setDespesas(BigDecimal v)       { this.despesas       = v; }
    public void setSaldo(BigDecimal v)          { this.saldo          = v; }
    public void setCardSpend(BigDecimal v)      { this.cardSpend      = v; }
    public void setSaldoAcumulado(BigDecimal v) { this.saldoAcumulado = v; }
    public void setIsFuture(boolean v)          { this.isFuture       = v; }
    public void setHasData(boolean v)           { this.hasData        = v; }
}
