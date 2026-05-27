package com.financeira.api.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class Payslip {

    private UUID id;
    private String userUid;
    private String competencia;
    private BigDecimal salarioBase;
    private BigDecimal inss;
    private BigDecimal irrf;
    private BigDecimal pensaoAlimenticia;
    private BigDecimal emprestimoConsignado;
    private BigDecimal assistenciaMedica;
    private BigDecimal coparticipacao;
    private BigDecimal pgbl;
    private BigDecimal seguroVida;
    private BigDecimal valeTransporte;
    private BigDecimal valeRefeicao;
    private BigDecimal fgts;
    private BigDecimal totalProventos;
    private BigDecimal totalDescontos;
    private BigDecimal liquido;
    private String observacoes;
    private List<PayslipItem> extras;
    private List<PayslipItem> outrosDescontos;

    public Payslip() {}

    public Payslip(String userUid, String competencia, BigDecimal salarioBase,
                   BigDecimal totalProventos, BigDecimal totalDescontos, BigDecimal liquido) {
        this.id = UUID.randomUUID();
        this.userUid = userUid;
        this.competencia = competencia;
        this.salarioBase = salarioBase;
        this.totalProventos = totalProventos;
        this.totalDescontos = totalDescontos;
        this.liquido = liquido;
    }

    public UUID getId()                         { return id; }
    public String getUserUid()                  { return userUid; }
    public String getCompetencia()              { return competencia; }
    public BigDecimal getSalarioBase()          { return salarioBase; }
    public BigDecimal getInss()                 { return inss; }
    public BigDecimal getIrrf()                 { return irrf; }
    public BigDecimal getPensaoAlimenticia()    { return pensaoAlimenticia; }
    public BigDecimal getEmprestimoConsignado() { return emprestimoConsignado; }
    public BigDecimal getAssistenciaMedica()    { return assistenciaMedica; }
    public BigDecimal getCoparticipacao()       { return coparticipacao; }
    public BigDecimal getPgbl()                 { return pgbl; }
    public BigDecimal getSeguroVida()           { return seguroVida; }
    public BigDecimal getValeTransporte()       { return valeTransporte; }
    public BigDecimal getValeRefeicao()         { return valeRefeicao; }
    public BigDecimal getFgts()                 { return fgts; }
    public BigDecimal getTotalProventos()       { return totalProventos; }
    public BigDecimal getTotalDescontos()       { return totalDescontos; }
    public BigDecimal getLiquido()              { return liquido; }
    public String getObservacoes()              { return observacoes; }
    public List<PayslipItem> getExtras()        { return extras; }
    public List<PayslipItem> getOutrosDescontos() { return outrosDescontos; }

    public void setId(UUID id)                              { this.id = id; }
    public void setUserUid(String u)                        { this.userUid = u; }
    public void setCompetencia(String c)                    { this.competencia = c; }
    public void setSalarioBase(BigDecimal v)                { this.salarioBase = v; }
    public void setInss(BigDecimal v)                       { this.inss = v; }
    public void setIrrf(BigDecimal v)                       { this.irrf = v; }
    public void setPensaoAlimenticia(BigDecimal v)          { this.pensaoAlimenticia = v; }
    public void setEmprestimoConsignado(BigDecimal v)       { this.emprestimoConsignado = v; }
    public void setAssistenciaMedica(BigDecimal v)          { this.assistenciaMedica = v; }
    public void setCoparticipacao(BigDecimal v)             { this.coparticipacao = v; }
    public void setPgbl(BigDecimal v)                       { this.pgbl = v; }
    public void setSeguroVida(BigDecimal v)                 { this.seguroVida = v; }
    public void setValeTransporte(BigDecimal v)             { this.valeTransporte = v; }
    public void setValeRefeicao(BigDecimal v)               { this.valeRefeicao = v; }
    public void setFgts(BigDecimal v)                       { this.fgts = v; }
    public void setTotalProventos(BigDecimal v)             { this.totalProventos = v; }
    public void setTotalDescontos(BigDecimal v)             { this.totalDescontos = v; }
    public void setLiquido(BigDecimal v)                    { this.liquido = v; }
    public void setObservacoes(String o)                    { this.observacoes = o; }
    public void setExtras(List<PayslipItem> e)              { this.extras = e; }
    public void setOutrosDescontos(List<PayslipItem> o)     { this.outrosDescontos = o; }
}
