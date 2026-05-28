package com.financeira.api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BillRequest(
        @NotBlank String name,
        @NotNull @Positive BigDecimal amount,
        @NotNull LocalDate dueDate,
        UUID categoryId,          // nullable — categoria é opcional
        Boolean paid,
        LocalDate paidDate,
        UUID bankId,
        String notes,
        String type               // "pagar" | "receber" — default: "pagar"
) {}
