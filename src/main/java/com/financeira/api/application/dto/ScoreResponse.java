package com.financeira.api.application.dto;

import java.util.List;

/**
 * Resposta de GET /api/score?monthKey=YYYY-MM
 *
 * Espelho de useHealthScore() em HealthScore.tsx.
 *
 * Score total = savingsScore + budgetScore + emergencyScore + goalScore (max 100)
 *   savingsScore  (0–40): taxa de poupança = (income − debitosReais) / income
 *   budgetScore   (0–30): aderência ao orçamento por categoria
 *   emergencyScore(0–20): reserva de emergência em meses de despesa
 *   goalScore     (0–10): metas ativas
 *
 * label:
 *   >= 85 → "Excelente"
 *   >= 70 → "Muito bom"
 *   >= 50 → "Regular"
 *   >= 30 → "Atenção"
 *   default → "Crítico"
 *
 * color (hex):
 *   >= 85 → #34d399
 *   >= 70 → #7c8dff
 *   >= 50 → #fbbf24
 *   >= 30 → #fb923c
 *   default → #f87171
 */
public class ScoreResponse {

    private String           monthKey;
    private int              score;
    private String           label;
    private String           color;
    private boolean          noData;
    private List<ScoreComponent> breakdown;

    public ScoreResponse() {}

    public String               getMonthKey()  { return monthKey; }
    public int                  getScore()     { return score; }
    public String               getLabel()     { return label; }
    public String               getColor()     { return color; }
    public boolean              isNoData()     { return noData; }
    public List<ScoreComponent> getBreakdown() { return breakdown; }

    public void setMonthKey(String v)               { this.monthKey  = v; }
    public void setScore(int v)                     { this.score     = v; }
    public void setLabel(String v)                  { this.label     = v; }
    public void setColor(String v)                  { this.color     = v; }
    public void setNoData(boolean v)                { this.noData    = v; }
    public void setBreakdown(List<ScoreComponent> v){ this.breakdown = v; }
}
