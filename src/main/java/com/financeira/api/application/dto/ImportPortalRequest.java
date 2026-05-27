package com.financeira.api.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ImportPortalRequest(
        @JsonProperty("state") ImportState state,
        @JsonProperty("entries") List<RawEntry> entries,
        @JsonProperty("cards") List<RawCard> cards,
        @JsonProperty("banks") List<RawBank> banks,
        @JsonProperty("investments") List<RawInvestment> investments,
        @JsonProperty("goals") List<RawGoal> goals,
        @JsonProperty("categories") List<RawCategory> categories,
        @JsonProperty("recurrences") List<RawRecurrence> recurrences,
        @JsonProperty("payslips") List<RawPayslip> payslips,
        @JsonProperty("bills") List<RawBill> bills
) {
    public List<RawEntry> resolvedEntries()       { return state != null ? state.entries()      : (entries      != null ? entries      : List.of()); }
    public List<RawCard> resolvedCards()          { return state != null ? state.cards()        : (cards        != null ? cards        : List.of()); }
    public List<RawBank> resolvedBanks()          { return state != null ? state.banks()        : (banks        != null ? banks        : List.of()); }
    public List<RawInvestment> resolvedInvestments() { return state != null ? state.investments() : (investments != null ? investments : List.of()); }
    public List<RawGoal> resolvedGoals()          { return state != null ? state.goals()        : (goals        != null ? goals        : List.of()); }
    public List<RawCategory> resolvedCategories() { return state != null ? state.categories()  : (categories   != null ? categories   : List.of()); }
    public List<RawRecurrence> resolvedRecurrences() { return state != null ? state.recurrences() : (recurrences != null ? recurrences : List.of()); }
    public List<RawPayslip> resolvedPayslips()    { return state != null ? state.payslips()    : (payslips     != null ? payslips     : List.of()); }
    public List<RawBill> resolvedBills()          { return state != null ? state.bills()       : (bills        != null ? bills        : List.of()); }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ImportState(
            List<RawEntry> entries,
            List<RawCard> cards,
            List<RawBank> banks,
            List<RawInvestment> investments,
            List<RawGoal> goals,
            List<RawCategory> categories,
            List<RawRecurrence> recurrences,
            List<RawPayslip> payslips,
            List<RawBill> bills
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawEntry(
            String id, String monthKey, String kind, String name, String category,
            Double amount, String date, String icon, String accountId,
            Integer installmentTotal, Integer installmentCurrent, String installmentGroupId,
            String recurrenceId, Integer recurrenceMonths, String cardId, String billingMonth,
            String invoiceRef, String toAccountId, Boolean isPaid, Boolean isReconciled,
            String notes, List<String> tags
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawCategory(
            String id, String name, String icon, Double budget, String color, String type, String nature
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawBank(
            String id, String name, String type, Double balance, String color, String icon
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawCard(
            String id, String name, String brand, String lastDigits, Double limit,
            Integer closingDay, Integer dueDay, String color, String icon, String bankId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawRecurrence(
            String id, String name, String icon, String category, String kind,
            Double amount, String cardId, String accountId, String startMonth, String endMonth,
            Integer months, Boolean active
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawBill(
            String id, String name, Double amount, String dueDate, String category,
            Boolean paid, String paidDate, String bankId, String notes, String type
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawInvestment(
            String id, String name, String type, Double amount, Double currentValue,
            Double rate, String maturity, String bankId, Boolean isEmergencyReserve,
            String icon, String color
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawGoal(
            String id, String name, String icon, Double targetAmount, Double currentAmount,
            String deadline, String color, String status, String notes
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawPayslip(
            String id, String competencia, Double salarioBase,
            List<RawPayslipItem> extras,
            Double inss, Double irrf, Double pensaoAlimenticia, Double emprestimoConsignado,
            Double assistenciaMedica, Double coparticipacao, Double pgbl, Double seguroVida,
            Double valeTransporte, Double valeRefeicao,
            List<RawPayslipItem> outrosDescontos,
            Double fgts, Double totalProventos, Double totalDescontos, Double liquido,
            String observacoes
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawPayslipItem(String descricao, Double valor) {}
}
