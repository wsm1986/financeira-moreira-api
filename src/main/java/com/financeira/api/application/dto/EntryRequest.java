package com.financeira.api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EntryRequest(
        @NotBlank String monthKey,
        @NotBlank String kind,
        @NotBlank String name,
        @NotNull UUID categoryId,
        @NotNull @Positive BigDecimal amount,
        @NotNull LocalDate entryDate,
        String icon,
        UUID accountId,
        Integer installmentTotal,
        Integer installmentCurrent,
        UUID installmentGroupId,
        UUID recurrenceId,
        Integer recurrenceMonths,
        UUID cardId,
        String billingMonth,
        String invoiceRef,
        UUID toAccountId,
        Boolean isPaid,
        Boolean isReconciled,
        String notes,
        List<String> tags
) {}
