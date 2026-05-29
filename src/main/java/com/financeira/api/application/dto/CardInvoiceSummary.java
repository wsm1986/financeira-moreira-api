package com.financeira.api.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resumo de fatura por cartão — componente de SummaryResponse.
 * fatura   = total gasto no cartão no mês anterior (billingMonth)
 * pago     = total de pagamento_fatura já lançado para este cartão no mês atual
 * pendente = MAX(0, fatura - pago)
 */
public class CardInvoiceSummary {

    private UUID   cardId;
    private String cardName;
    private BigDecimal fatura;
    private BigDecimal pago;
    private BigDecimal pendente;

    public CardInvoiceSummary() {}

    public CardInvoiceSummary(UUID cardId, String cardName,
                              BigDecimal fatura, BigDecimal pago, BigDecimal pendente) {
        this.cardId   = cardId;
        this.cardName = cardName;
        this.fatura   = fatura;
        this.pago     = pago;
        this.pendente = pendente;
    }

    public UUID       getCardId()   { return cardId; }
    public String     getCardName() { return cardName; }
    public BigDecimal getFatura()   { return fatura; }
    public BigDecimal getPago()     { return pago; }
    public BigDecimal getPendente() { return pendente; }
}
