package com.financeira.api.application.dto;

import com.financeira.api.domain.model.Payslip;
import com.financeira.api.domain.model.PayslipItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record PayslipResponse(
        UUID id,
        String competencia,
        BigDecimal salarioBase,
        List<PayslipItemDto> extras,
        BigDecimal inss,
        BigDecimal irrf,
        BigDecimal pensaoAlimenticia,
        BigDecimal emprestimoConsignado,
        BigDecimal assistenciaMedica,
        BigDecimal coparticipacao,
        BigDecimal pgbl,
        BigDecimal seguroVida,
        BigDecimal valeTransporte,
        BigDecimal valeRefeicao,
        List<PayslipItemDto> outrosDescontos,
        BigDecimal fgts,
        BigDecimal totalProventos,
        BigDecimal totalDescontos,
        BigDecimal liquido,
        String observacoes
) {
    public static PayslipResponse from(Payslip p) {
        List<PayslipItemDto> extras = p.getExtras() != null
                ? p.getExtras().stream().map(i -> new PayslipItemDto(i.getDescricao(), i.getValor())).collect(Collectors.toList())
                : List.of();
        List<PayslipItemDto> outrosDescontos = p.getOutrosDescontos() != null
                ? p.getOutrosDescontos().stream().map(i -> new PayslipItemDto(i.getDescricao(), i.getValor())).collect(Collectors.toList())
                : List.of();
        return new PayslipResponse(p.getId(), p.getCompetencia(), p.getSalarioBase(),
                extras, p.getInss(), p.getIrrf(), p.getPensaoAlimenticia(),
                p.getEmprestimoConsignado(), p.getAssistenciaMedica(), p.getCoparticipacao(),
                p.getPgbl(), p.getSeguroVida(), p.getValeTransporte(), p.getValeRefeicao(),
                outrosDescontos, p.getFgts(), p.getTotalProventos(), p.getTotalDescontos(),
                p.getLiquido(), p.getObservacoes());
    }
}
