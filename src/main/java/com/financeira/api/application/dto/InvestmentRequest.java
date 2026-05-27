package com.financeira.api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvestmentRequest(
        @NotBlank String name,
        @NotBlank String type,
        @NotNull BigDecimal amount,
        @NotNull BigDecimal currentValue,
        BigDecimal rate,
        LocalDate maturity,
        UUID bankId,
        Boolean isEmergencyReserve,
        String icon,
        String color
) {}
