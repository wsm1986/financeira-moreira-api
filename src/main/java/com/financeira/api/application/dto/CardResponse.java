package com.financeira.api.application.dto;

import com.financeira.api.domain.model.CreditCard;

import java.math.BigDecimal;
import java.util.UUID;

public record CardResponse(
        UUID id,
        String name,
        String brand,
        String lastDigits,
        BigDecimal cardLimit,
        Integer closingDay,
        Integer dueDay,
        String color,
        String icon,
        UUID bankId
) {
    public static CardResponse from(CreditCard c) {
        return new CardResponse(c.getId(), c.getName(), c.getBrand(), c.getLastDigits(),
                c.getCardLimit(), c.getClosingDay(), c.getDueDay(), c.getColor(), c.getIcon(), c.getBankId());
    }
}
