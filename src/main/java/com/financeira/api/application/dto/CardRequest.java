package com.financeira.api.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CardRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "visa|mastercard|elo|amex|hipercard") String brand,
        @NotBlank @Size(min = 4, max = 4) String lastDigits,
        @NotNull @Positive BigDecimal cardLimit,
        @Min(1) @Max(28) Integer closingDay,
        @Min(1) @Max(28) Integer dueDay,
        String color,
        String icon,
        UUID bankId
) {}
