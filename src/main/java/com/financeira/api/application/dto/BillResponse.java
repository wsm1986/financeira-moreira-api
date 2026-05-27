package com.financeira.api.application.dto;

import com.financeira.api.domain.model.Bill;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BillResponse(
        UUID id,
        String name,
        BigDecimal amount,
        LocalDate dueDate,
        UUID categoryId,
        Boolean paid,
        LocalDate paidDate,
        UUID bankId,
        String notes,
        String type
) {
    public static BillResponse from(Bill b) {
        return new BillResponse(b.getId(), b.getName(), b.getAmount(), b.getDueDate(),
                b.getCategoryId(), b.getPaid(), b.getPaidDate(), b.getBankId(), b.getNotes(), b.getType());
    }
}
