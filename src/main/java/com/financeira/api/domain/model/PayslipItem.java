package com.financeira.api.domain.model;

import java.math.BigDecimal;

public class PayslipItem {

    private String type;
    private String descricao;
    private BigDecimal valor;

    public PayslipItem() {}

    public PayslipItem(String type, String descricao, BigDecimal valor) {
        this.type = type;
        this.descricao = descricao;
        this.valor = valor;
    }

    public String getType()        { return type; }
    public String getDescricao()   { return descricao; }
    public BigDecimal getValor()   { return valor; }

    public void setType(String t)        { this.type = t; }
    public void setDescricao(String d)   { this.descricao = d; }
    public void setValor(BigDecimal v)   { this.valor = v; }
}
