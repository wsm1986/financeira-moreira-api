package com.financeira.api.infrastructure.persistence.entity;

import com.financeira.api.domain.model.PayslipItem;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payslip_items")
public class PayslipItemEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payslip_id", nullable = false)
    private PayslipEntity payslip;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    public PayslipItemEntity() {}

    public static PayslipItemEntity fromDomain(PayslipItem item, PayslipEntity payslip) {
        PayslipItemEntity e = new PayslipItemEntity();
        e.id = UUID.randomUUID();
        e.payslip = payslip;
        e.type = item.getType();
        e.descricao = item.getDescricao();
        e.valor = item.getValor();
        return e;
    }

    public PayslipItem toDomain() {
        return new PayslipItem(type, descricao, valor);
    }

    public UUID getId()            { return id; }
    public PayslipEntity getPayslip() { return payslip; }
    public String getType()        { return type; }
    public String getDescricao()   { return descricao; }
    public BigDecimal getValor()   { return valor; }

    public void setId(UUID id)               { this.id = id; }
    public void setPayslip(PayslipEntity p)  { this.payslip = p; }
    public void setType(String t)            { this.type = t; }
    public void setDescricao(String d)       { this.descricao = d; }
    public void setValor(BigDecimal v)       { this.valor = v; }
}
