package com.financeira.api.application.dto;

import com.financeira.api.domain.model.Goal;

import java.math.BigDecimal;
import java.util.UUID;

public record GoalResponse(
        UUID id,
        String name,
        String icon,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        String deadline,
        String color,
        String status,
        String notes
) {
    public static GoalResponse from(Goal g) {
        return new GoalResponse(g.getId(), g.getName(), g.getIcon(), g.getTargetAmount(),
                g.getCurrentAmount(), g.getDeadline(), g.getColor(), g.getStatus(), g.getNotes());
    }
}
