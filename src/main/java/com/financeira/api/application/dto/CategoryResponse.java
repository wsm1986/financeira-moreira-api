package com.financeira.api.application.dto;

import com.financeira.api.domain.model.Category;
import java.math.BigDecimal;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String icon,
        BigDecimal budget,
        String color,
        String type,
        String nature
) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getIcon(),
                c.getBudget(),
                c.getColor(),
                c.getType(),
                c.getNature()
        );
    }
}
