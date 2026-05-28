package com.financeira.api.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record RecurrenceRequest(
        @NotBlank String name,
        String icon,
        UUID categoryId,          // nullable — categoria é opcional
        @JsonAlias("frequency") @NotBlank String kind,   // portal manda "frequency", backend usa "kind"
        @NotNull @Positive BigDecimal amount,
        UUID cardId,
        UUID accountId,
        @NotBlank String startMonth,
        String endMonth,
        @NotNull Integer months,
        Boolean active
) {}
