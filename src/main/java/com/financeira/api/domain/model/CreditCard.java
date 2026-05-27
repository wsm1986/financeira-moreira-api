package com.financeira.api.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class CreditCard {

    private UUID id;
    private String userUid;
    private String name;
    private String brand;
    private String lastDigits;
    private BigDecimal cardLimit;
    private Integer closingDay;
    private Integer dueDay;
    private String color;
    private String icon;
    private UUID bankId;

    public CreditCard() {}

    public CreditCard(String userUid, String name, String brand, String lastDigits,
                      BigDecimal cardLimit, Integer closingDay, Integer dueDay,
                      String color, String icon, UUID bankId) {
        this.id = UUID.randomUUID();
        this.userUid = userUid;
        this.name = name;
        this.brand = brand;
        this.lastDigits = lastDigits;
        this.cardLimit = cardLimit;
        this.closingDay = closingDay;
        this.dueDay = dueDay;
        this.color = color;
        this.icon = icon;
        this.bankId = bankId;
    }

    public UUID getId()              { return id; }
    public String getUserUid()       { return userUid; }
    public String getName()          { return name; }
    public String getBrand()         { return brand; }
    public String getLastDigits()    { return lastDigits; }
    public BigDecimal getCardLimit() { return cardLimit; }
    public Integer getClosingDay()   { return closingDay; }
    public Integer getDueDay()       { return dueDay; }
    public String getColor()         { return color; }
    public String getIcon()          { return icon; }
    public UUID getBankId()          { return bankId; }

    public void setId(UUID id)                   { this.id = id; }
    public void setUserUid(String userUid)       { this.userUid = userUid; }
    public void setName(String name)             { this.name = name; }
    public void setBrand(String brand)           { this.brand = brand; }
    public void setLastDigits(String d)          { this.lastDigits = d; }
    public void setCardLimit(BigDecimal l)       { this.cardLimit = l; }
    public void setClosingDay(Integer d)         { this.closingDay = d; }
    public void setDueDay(Integer d)             { this.dueDay = d; }
    public void setColor(String color)           { this.color = color; }
    public void setIcon(String icon)             { this.icon = icon; }
    public void setBankId(UUID bankId)           { this.bankId = bankId; }
}
