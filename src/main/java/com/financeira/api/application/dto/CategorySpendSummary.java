package com.financeira.api.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Gasto real por categoria no mês — componente de SummaryResponse.
 * Inclui qualquer kind que seja gasto (exclui receita, pagamento_fatura, transferencia).
 */
public class CategorySpendSummary {

    private UUID       categoryId;
    private String     categoryName;
    private BigDecimal budget;
    private BigDecimal gasto;
    /** pct = gasto / budget * 100  (0 se budget == 0) */
    private double     pct;

    public CategorySpendSummary() {}

    public CategorySpendSummary(UUID categoryId, String categoryName,
                                BigDecimal budget, BigDecimal gasto, double pct) {
        this.categoryId   = categoryId;
        this.categoryName = categoryName;
        this.budget       = budget;
        this.gasto        = gasto;
        this.pct          = pct;
    }

    public UUID       getCategoryId()   { return categoryId; }
    public String     getCategoryName() { return categoryName; }
    public BigDecimal getBudget()       { return budget; }
    public BigDecimal getGasto()        { return gasto; }
    public double     getPct()          { return pct; }
}
