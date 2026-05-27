package com.financeira.api.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Recurrence {

    private UUID id;
    private String userUid;
    private String name;
    private String icon;
    private UUID categoryId;
    private String kind;
    private BigDecimal amount;
    private UUID cardId;
    private UUID accountId;
    private String startMonth;
    private String endMonth;
    private Integer months;
    private Boolean active;

    public Recurrence() {}

    public Recurrence(String userUid, String name, String icon, UUID categoryId, String kind,
                      BigDecimal amount, UUID cardId, UUID accountId, String startMonth,
                      String endMonth, Integer months, Boolean active) {
        this.id = UUID.randomUUID();
        this.userUid = userUid;
        this.name = name;
        this.icon = icon;
        this.categoryId = categoryId;
        this.kind = kind;
        this.amount = amount;
        this.cardId = cardId;
        this.accountId = accountId;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        this.months = months;
        this.active = active != null ? active : true;
    }

    public UUID getId()           { return id; }
    public String getUserUid()    { return userUid; }
    public String getName()       { return name; }
    public String getIcon()       { return icon; }
    public UUID getCategoryId()   { return categoryId; }
    public String getKind()       { return kind; }
    public BigDecimal getAmount() { return amount; }
    public UUID getCardId()       { return cardId; }
    public UUID getAccountId()    { return accountId; }
    public String getStartMonth() { return startMonth; }
    public String getEndMonth()   { return endMonth; }
    public Integer getMonths()    { return months; }
    public Boolean getActive()    { return active; }

    public void setId(UUID id)              { this.id = id; }
    public void setUserUid(String u)        { this.userUid = u; }
    public void setName(String n)           { this.name = n; }
    public void setIcon(String i)           { this.icon = i; }
    public void setCategoryId(UUID c)       { this.categoryId = c; }
    public void setKind(String k)           { this.kind = k; }
    public void setAmount(BigDecimal a)     { this.amount = a; }
    public void setCardId(UUID c)           { this.cardId = c; }
    public void setAccountId(UUID a)        { this.accountId = a; }
    public void setStartMonth(String s)     { this.startMonth = s; }
    public void setEndMonth(String e)       { this.endMonth = e; }
    public void setMonths(Integer m)        { this.months = m; }
    public void setActive(Boolean a)        { this.active = a; }
}
