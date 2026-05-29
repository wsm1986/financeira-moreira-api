package com.financeira.api.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resposta de GET /api/summary/annual?year=YYYY
 *
 * Espelho de ytdData em Analysis.tsx (useMemo sobre store.entries).
 *
 * Contém os 12 meses do ano com os dados de cada mês e totais anuais.
 * Meses históricos usam calcDebitosReais; meses futuros usam calcTotalDespesas.
 */
public class AnnualSummaryResponse {

    private int  year;
    /** 12 linhas Jan–Dez */
    private List<AnnualMonthRow> months;

    // ── Totais anuais (apenas meses históricos com dados) ──────────────
    private BigDecimal totalReceita;
    private BigDecimal totalDespesas;
    private BigDecimal saldo;
    /** savingsRate = totalReceita > 0 ? saldo / totalReceita * 100 : 0 */
    private double     savingsRate;
    private BigDecimal avgMonthlyReceita;
    private BigDecimal avgMonthlyDespesas;
    /** Número de meses com dados (denominador das médias) */
    private int        monthsWithData;

    public AnnualSummaryResponse() {}

    public int                   getYear()               { return year; }
    public List<AnnualMonthRow>  getMonths()             { return months; }
    public BigDecimal            getTotalReceita()        { return totalReceita; }
    public BigDecimal            getTotalDespesas()       { return totalDespesas; }
    public BigDecimal            getSaldo()               { return saldo; }
    public double                getSavingsRate()         { return savingsRate; }
    public BigDecimal            getAvgMonthlyReceita()   { return avgMonthlyReceita; }
    public BigDecimal            getAvgMonthlyDespesas()  { return avgMonthlyDespesas; }
    public int                   getMonthsWithData()      { return monthsWithData; }

    public void setYear(int v)                          { this.year               = v; }
    public void setMonths(List<AnnualMonthRow> v)       { this.months             = v; }
    public void setTotalReceita(BigDecimal v)           { this.totalReceita       = v; }
    public void setTotalDespesas(BigDecimal v)          { this.totalDespesas      = v; }
    public void setSaldo(BigDecimal v)                  { this.saldo              = v; }
    public void setSavingsRate(double v)                { this.savingsRate        = v; }
    public void setAvgMonthlyReceita(BigDecimal v)      { this.avgMonthlyReceita  = v; }
    public void setAvgMonthlyDespesas(BigDecimal v)     { this.avgMonthlyDespesas = v; }
    public void setMonthsWithData(int v)                { this.monthsWithData     = v; }
}
