package com.financeira.api.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Bill {

    private UUID id;
    private String userUid;
    private String name;
    private BigDecimal amount;
    private LocalDate dueDate;
    private UUID categoryId;
    private Boolean paid;
    private LocalDate paidDate;
    private UUID bankId;
    private String notes;
    private String type;

    public Bill() {}

    public Bill(String userUid, String name, BigDecimal amount, LocalDate dueDate,
                UUID categoryId, Boolean paid, LocalDate paidDate, UUID bankId, String notes, String type) {
        this.id = UUID.randomUUID();
        this.userUid = userUid;
        this.name = name;
        this.amount = amount;
        this.dueDate = dueDate;
        this.categoryId = categoryId;
        this.paid = paid != null ? paid : false;
        this.paidDate = paidDate;
        this.bankId = bankId;
        this.notes = notes;
        this.type = type;
    }

    public UUID getId()           { return id; }
    public String getUserUid()    { return userUid; }
    public String getName()       { return name; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDueDate() { return dueDate; }
    public UUID getCategoryId()   { return categoryId; }
    public Boolean getPaid()      { return paid; }
    public LocalDate getPaidDate(){ return paidDate; }
    public UUID getBankId()       { return bankId; }
    public String getNotes()      { return notes; }
    public String getType()       { return type; }

    public void setId(UUID id)              { this.id = id; }
    public void setUserUid(String u)        { this.userUid = u; }
    public void setName(String n)           { this.name = n; }
    public void setAmount(BigDecimal a)     { this.amount = a; }
    public void setDueDate(LocalDate d)     { this.dueDate = d; }
    public void setCategoryId(UUID c)       { this.categoryId = c; }
    public void setPaid(Boolean p)          { this.paid = p; }
    public void setPaidDate(LocalDate d)    { this.paidDate = d; }
    public void setBankId(UUID b)           { this.bankId = b; }
    public void setNotes(String n)          { this.notes = n; }
    public void setType(String t)           { this.type = t; }
}
