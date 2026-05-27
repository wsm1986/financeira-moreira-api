package com.financeira.api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record PayslipRequest(
        @NotBlank String competencia,
        @NotNull BigDecimal salarioBase,
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
        @NotNull BigDecimal totalProventos,
        @NotNull BigDecimal totalDescontos,
        @NotNull BigDecimal liquido,
        String observacoes
) {}
