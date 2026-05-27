package com.financeira.api.application.dto;

import com.financeira.api.domain.model.Entry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EntryResponse(
        UUID id,
        String monthKey,
        String kind,
        String name,
        UUID categoryId,
        BigDecimal amount,
        LocalDate entryDate,
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
) {
    public static EntryResponse from(Entry e) {
        return new EntryResponse(
                e.getId(), e.getMonthKey(), e.getKind(), e.getName(),
                e.getCategoryId(), e.getAmount(), e.getEntryDate(), e.getIcon(),
                e.getAccountId(), e.getInstallmentTotal(), e.getInstallmentCurrent(),
                e.getInstallmentGroupId(), e.getRecurrenceId(), e.getRecurrenceMonths(),
                e.getCardId(), e.getBillingMonth(), e.getInvoiceRef(), e.getToAccountId(),
                e.getIsPaid(), e.getIsReconciled(), e.getNotes(), e.getTags()
        );
    }
}
