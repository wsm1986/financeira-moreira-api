package com.financeira.api.application.dto;

import com.financeira.api.domain.model.Bank;

import java.math.BigDecimal;
import java.util.UUID;

public record BankResponse(
        UUID id,
        String name,
        String type,
        BigDecimal balance,
        String color,
        String icon
) {
    public static BankResponse from(Bank b) {
        return new BankResponse(b.getId(), b.getName(), b.getType(), b.getBalance(), b.getColor(), b.getIcon());
    }
}
