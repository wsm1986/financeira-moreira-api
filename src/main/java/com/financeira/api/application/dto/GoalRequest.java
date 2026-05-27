package com.financeira.api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record GoalRequest(
        @NotBlank String name,
        String icon,
        @NotNull BigDecimal targetAmount,
        BigDecimal currentAmount,
        @NotBlank String deadline,
        String color,
        String status,
        String notes
) {}
