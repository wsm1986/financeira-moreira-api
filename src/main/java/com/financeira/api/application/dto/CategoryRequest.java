package com.financeira.api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record CategoryRequest(
        @NotBlank(message = "name é obrigatório") String name,
        String icon,
        BigDecimal budget,
        String color,
        @NotBlank(message = "type é obrigatório")
        @Pattern(regexp = "expense|income|both", message = "type deve ser expense, income ou both")
        String type,
        @Pattern(regexp = "essencial|desejo|investimento|", message = "nature inválido")
        String nature
) {}
