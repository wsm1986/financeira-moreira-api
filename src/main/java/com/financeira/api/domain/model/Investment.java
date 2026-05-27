package com.financeira.api.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Investment {

    private UUID id;
    private String userUid;
    private String name;
    private String type;
    private BigDecimal amount;
    private BigDecimal currentValue;
    private BigDecimal rate;
    private LocalDate maturity;
    private UUID bankId;
    private Boolean isEmergencyReserve;
    private String icon;
    private String color;

    public Investment() {}

    public Investment(String userUid, String name, String type, BigDecimal amount, BigDecimal currentValue,
                      BigDecimal rate, LocalDate maturity, UUID bankId, Boolean isEmergencyReserve,
                      String icon, String color) {
        this.id = UUID.randomUUID();
        this.userUid = userUid;
        this.name = name;
        this.type = type;
        this.amount = amount;
        this.currentValue = currentValue;
        this.rate = rate;
        this.maturity = maturity;
        this.bankId = bankId;
        this.isEmergencyReserve = isEmergencyReserve != null ? isEmergencyReserve : false;
        this.icon = icon;
        this.color = color;
    }

    public UUID getId()                    { return id; }
    public String getUserUid()             { return userUid; }
    public String getName()                { return name; }
    public String getType()                { return type; }
    public BigDecimal getAmount()          { return amount; }
    public BigDecimal getCurrentValue()    { return currentValue; }
    public BigDecimal getRate()            { return rate; }
    public LocalDate getMaturity()         { return maturity; }
    public UUID getBankId()                { return bankId; }
    public Boolean getIsEmergencyReserve() { return isEmergencyReserve; }
    public String getIcon()                { return icon; }
    public String getColor()               { return color; }

    public void setId(UUID id)                        { this.id = id; }
    public void setUserUid(String u)                  { this.userUid = u; }
    public void setName(String n)                     { this.name = n; }
    public void setType(String t)                     { this.type = t; }
    public void setAmount(BigDecimal a)               { this.amount = a; }
    public void setCurrentValue(BigDecimal v)         { this.currentValue = v; }
    public void setRate(BigDecimal r)                 { this.rate = r; }
    public void setMaturity(LocalDate m)              { this.maturity = m; }
    public void setBankId(UUID b)                     { this.bankId = b; }
    public void setIsEmergencyReserve(Boolean e)      { this.isEmergencyReserve = e; }
    public void setIcon(String i)                     { this.icon = i; }
    public void setColor(String c)                    { this.color = c; }
}
