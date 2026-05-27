package com.financeira.api.infrastructure.persistence.entity;

import com.financeira.api.domain.model.Payslip;
import com.financeira.api.domain.model.PayslipItem;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "payslips")
@SQLRestriction("deleted_at IS NULL")
public class PayslipEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_uid", nullable = false)
    private String userUid;

    @Column(nullable = false, length = 7)
    private String competencia;

    @Column(name = "salario_base", nullable = false, precision = 19, scale = 2)
    private BigDecimal salarioBase;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal inss = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal irrf = BigDecimal.ZERO;

    @Column(name = "pensao_alimenticia", nullable = false, precision = 19, scale = 2)
    private BigDecimal pensaoAlimenticia = BigDecimal.ZERO;

    @Column(name = "emprestimo_consignado", nullable = false, precision = 19, scale = 2)
    private BigDecimal emprestimoConsignado = BigDecimal.ZERO;

    @Column(name = "assistencia_medica", nullable = false, precision = 19, scale = 2)
    private BigDecimal assistenciaMedica = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal coparticipacao = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal pgbl = BigDecimal.ZERO;

    @Column(name = "seguro_vida", nullable = false, precision = 19, scale = 2)
    private BigDecimal seguroVida = BigDecimal.ZERO;

    @Column(name = "vale_transporte", nullable = false, precision = 19, scale = 2)
    private BigDecimal valeTransporte = BigDecimal.ZERO;

    @Column(name = "vale_refeicao", nullable = false, precision = 19, scale = 2)
    private BigDecimal valeRefeicao = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fgts = BigDecimal.ZERO;

    @Column(name = "total_proventos", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalProventos;

    @Column(name = "total_descontos", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalDescontos;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal liquido;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @OneToMany(mappedBy = "payslip", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PayslipItemEntity> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public PayslipEntity() {}

    public static PayslipEntity fromDomain(Payslip p) {
        PayslipEntity e = new PayslipEntity();
        e.id = p.getId();
        e.userUid = p.getUserUid();
        e.competencia = p.getCompetencia();
        e.salarioBase = p.getSalarioBase();
        e.inss = orZero(p.getInss());
        e.irrf = orZero(p.getIrrf());
        e.pensaoAlimenticia = orZero(p.getPensaoAlimenticia());
        e.emprestimoConsignado = orZero(p.getEmprestimoConsignado());
        e.assistenciaMedica = orZero(p.getAssistenciaMedica());
        e.coparticipacao = orZero(p.getCoparticipacao());
        e.pgbl = orZero(p.getPgbl());
        e.seguroVida = orZero(p.getSeguroVida());
        e.valeTransporte = orZero(p.getValeTransporte());
        e.valeRefeicao = orZero(p.getValeRefeicao());
        e.fgts = orZero(p.getFgts());
        e.totalProventos = p.getTotalProventos();
        e.totalDescontos = p.getTotalDescontos();
        e.liquido = p.getLiquido();
        e.observacoes = p.getObservacoes();
        if (p.getExtras() != null) {
            for (PayslipItem item : p.getExtras()) {
                PayslipItemEntity ie = PayslipItemEntity.fromDomain(item, e);
                ie.setType("extra");
                e.items.add(ie);
            }
        }
        if (p.getOutrosDescontos() != null) {
            for (PayslipItem item : p.getOutrosDescontos()) {
                PayslipItemEntity ie = PayslipItemEntity.fromDomain(item, e);
                ie.setType("desconto");
                e.items.add(ie);
            }
        }
        return e;
    }

    public Payslip toDomain() {
        Payslip p = new Payslip(userUid, competencia, salarioBase, totalProventos, totalDescontos, liquido);
        p.setId(id);
        p.setInss(inss);
        p.setIrrf(irrf);
        p.setPensaoAlimenticia(pensaoAlimenticia);
        p.setEmprestimoConsignado(emprestimoConsignado);
        p.setAssistenciaMedica(assistenciaMedica);
        p.setCoparticipacao(coparticipacao);
        p.setPgbl(pgbl);
        p.setSeguroVida(seguroVida);
        p.setValeTransporte(valeTransporte);
        p.setValeRefeicao(valeRefeicao);
        p.setFgts(fgts);
        p.setObservacoes(observacoes);
        p.setExtras(items.stream().filter(i -> "extra".equals(i.getType()))
                .map(PayslipItemEntity::toDomain).collect(Collectors.toList()));
        p.setOutrosDescontos(items.stream().filter(i -> "desconto".equals(i.getType()))
                .map(PayslipItemEntity::toDomain).collect(Collectors.toList()));
        return p;
    }

    private static BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    public UUID getId()            { return id; }
    public String getUserUid()     { return userUid; }
    public String getCompetencia() { return competencia; }
    public Instant getDeletedAt()  { return deletedAt; }
    public List<PayslipItemEntity> getItems() { return items; }

    public void setDeletedAt(Instant t) { this.deletedAt = t; }
}
