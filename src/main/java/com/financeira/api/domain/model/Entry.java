package com.financeira.api.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class Entry {

    private UUID id;
    private String userUid;
    private String monthKey;
    private String kind;
    private String name;
    private UUID categoryId;
    private BigDecimal amount;
    private LocalDate entryDate;
    private String icon;
    private UUID accountId;
    private Integer installmentTotal;
    private Integer installmentCurrent;
    private UUID installmentGroupId;
    private UUID recurrenceId;
    private Integer recurrenceMonths;
    private UUID cardId;
    private String billingMonth;
    private String invoiceRef;
    private UUID toAccountId;
    private Boolean isPaid;
    private Boolean isReconciled;
    private String notes;
    private List<String> tags;

    public Entry() {}

    public Entry(String userUid, String monthKey, String kind, String name,
                 UUID categoryId, BigDecimal amount, LocalDate entryDate, String icon) {
        this.id = UUID.randomUUID();
        this.userUid = userUid;
        this.monthKey = monthKey;
        this.kind = kind;
        this.name = name;
        this.categoryId = categoryId;
        this.amount = amount;
        this.entryDate = entryDate;
        this.icon = icon;
        this.isPaid = false;
        this.isReconciled = false;
    }

    public UUID getId()                    { return id; }
    public String getUserUid()             { return userUid; }
    public String getMonthKey()            { return monthKey; }
    public String getKind()                { return kind; }
    public String getName()                { return name; }
    public UUID getCategoryId()            { return categoryId; }
    public BigDecimal getAmount()          { return amount; }
    public LocalDate getEntryDate()        { return entryDate; }
    public String getIcon()                { return icon; }
    public UUID getAccountId()             { return accountId; }
    public Integer getInstallmentTotal()   { return installmentTotal; }
    public Integer getInstallmentCurrent() { return installmentCurrent; }
    public UUID getInstallmentGroupId()    { return installmentGroupId; }
    public UUID getRecurrenceId()          { return recurrenceId; }
    public Integer getRecurrenceMonths()   { return recurrenceMonths; }
    public UUID getCardId()                { return cardId; }
    public String getBillingMonth()        { return billingMonth; }
    public String getInvoiceRef()          { return invoiceRef; }
    public UUID getToAccountId()           { return toAccountId; }
    public Boolean getIsPaid()             { return isPaid; }
    public Boolean getIsReconciled()       { return isReconciled; }
    public String getNotes()               { return notes; }
    public List<String> getTags()          { return tags; }

    public void setId(UUID id)                        { this.id = id; }
    public void setUserUid(String u)                  { this.userUid = u; }
    public void setMonthKey(String m)                 { this.monthKey = m; }
    public void setKind(String k)                     { this.kind = k; }
    public void setName(String n)                     { this.name = n; }
    public void setCategoryId(UUID c)                 { this.categoryId = c; }
    public void setAmount(BigDecimal a)               { this.amount = a; }
    public void setEntryDate(LocalDate d)             { this.entryDate = d; }
    public void setIcon(String i)                     { this.icon = i; }
    public void setAccountId(UUID a)                  { this.accountId = a; }
    public void setInstallmentTotal(Integer t)        { this.installmentTotal = t; }
    public void setInstallmentCurrent(Integer c)      { this.installmentCurrent = c; }
    public void setInstallmentGroupId(UUID g)         { this.installmentGroupId = g; }
    public void setRecurrenceId(UUID r)               { this.recurrenceId = r; }
    public void setRecurrenceMonths(Integer m)        { this.recurrenceMonths = m; }
    public void setCardId(UUID c)                     { this.cardId = c; }
    public void setBillingMonth(String b)             { this.billingMonth = b; }
    public void setInvoiceRef(String r)               { this.invoiceRef = r; }
    public void setToAccountId(UUID t)                { this.toAccountId = t; }
    public void setIsPaid(Boolean p)                  { this.isPaid = p; }
    public void setIsReconciled(Boolean r)            { this.isReconciled = r; }
    public void setNotes(String n)                    { this.notes = n; }
    public void setTags(List<String> t)               { this.tags = t; }
}
