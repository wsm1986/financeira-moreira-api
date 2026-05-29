package com.financeira.api.application.dto;

/**
 * Componente individual do Score Financeiro.
 * Espelho de ScoreBreakdown em HealthScore.tsx.
 */
public class ScoreComponent {

    private String label;
    private int    score;
    private int    max;
    private String detail;
    private String color;

    public ScoreComponent() {}

    public ScoreComponent(String label, int score, int max, String detail, String color) {
        this.label  = label;
        this.score  = score;
        this.max    = max;
        this.detail = detail;
        this.color  = color;
    }

    public String getLabel()  { return label; }
    public int    getScore()  { return score; }
    public int    getMax()    { return max; }
    public String getDetail() { return detail; }
    public String getColor()  { return color; }

    public void setLabel(String v)  { this.label  = v; }
    public void setScore(int v)     { this.score  = v; }
    public void setMax(int v)       { this.max    = v; }
    public void setDetail(String v) { this.detail = v; }
    public void setColor(String v)  { this.color  = v; }
}
