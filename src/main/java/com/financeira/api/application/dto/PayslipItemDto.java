package com.financeira.api.application.dto;

import java.math.BigDecimal;

public record PayslipItemDto(String descricao, BigDecimal valor) {}
