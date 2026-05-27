package com.financeira.api.application.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record BankRequest(
        @NotBlank String name,
        @NotBlank String type,
        BigDecimal balance,
        String color,
        String icon
) {}
