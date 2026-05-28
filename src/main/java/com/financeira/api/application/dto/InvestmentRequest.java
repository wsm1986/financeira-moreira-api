package com.financeira.api.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvestmentRequest(
        @NotBlank String name,
        @NotBlank String type,
        @NotNull BigDecimal amount,
        @JsonAlias("returns") BigDecimal currentValue,  // portal manda "returns", API direta manda "currentValue"
        String rateStr,                                  // portal manda "rate": "CDI 120%" (string) → ignorado no cálculo
        @JsonAlias({"date", "startDate"}) LocalDate maturity,  // portal manda "date", backend usa "maturity"
        UUID bankId,
        Boolean isEmergencyReserve,
        String icon,
        String color
) {}
