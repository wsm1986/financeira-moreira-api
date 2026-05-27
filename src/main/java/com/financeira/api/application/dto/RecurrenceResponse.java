package com.financeira.api.application.dto;

import com.financeira.api.domain.model.Recurrence;

import java.math.BigDecimal;
import java.util.UUID;

public record RecurrenceResponse(
        UUID id,
        String name,
        String icon,
        UUID categoryId,
        String kind,
        BigDecimal amount,
        UUID cardId,
        UUID accountId,
        String startMonth,
        String endMonth,
        Integer months,
        Boolean active
) {
    public static RecurrenceResponse from(Recurrence r) {
        return new RecurrenceResponse(r.getId(), r.getName(), r.getIcon(), r.getCategoryId(),
                r.getKind(), r.getAmount(), r.getCardId(), r.getAccountId(),
                r.getStartMonth(), r.getEndMonth(), r.getMonths(), r.getActive());
    }
}
