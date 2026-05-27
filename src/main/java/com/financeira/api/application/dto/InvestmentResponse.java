package com.financeira.api.application.dto;

import com.financeira.api.domain.model.Investment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvestmentResponse(
        UUID id,
        String name,
        String type,
        BigDecimal amount,
        BigDecimal currentValue,
        BigDecimal rate,
        LocalDate maturity,
        UUID bankId,
        Boolean isEmergencyReserve,
        String icon,
        String color
) {
    public static InvestmentResponse from(Investment inv) {
        return new InvestmentResponse(inv.getId(), inv.getName(), inv.getType(),
                inv.getAmount(), inv.getCurrentValue(), inv.getRate(), inv.getMaturity(),
                inv.getBankId(), inv.getIsEmergencyReserve(), inv.getIcon(), inv.getColor());
    }
}
