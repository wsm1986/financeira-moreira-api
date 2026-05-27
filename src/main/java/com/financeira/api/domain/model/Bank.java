package com.financeira.api.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Bank {

    private UUID id;
    private String userUid;
    private String name;
    private String type;
    private BigDecimal balance;
    private String color;
    private String icon;

    public Bank() {}

    public Bank(String userUid, String name, String type, BigDecimal balance, String color, String icon) {
        this.id = UUID.randomUUID();
        this.userUid = userUid;
        this.name = name;
        this.type = type;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.color = color;
        this.icon = icon;
    }

    public UUID getId()            { return id; }
    public String getUserUid()     { return userUid; }
    public String getName()        { return name; }
    public String getType()        { return type; }
    public BigDecimal getBalance() { return balance; }
    public String getColor()       { return color; }
    public String getIcon()        { return icon; }

    public void setId(UUID id)               { this.id = id; }
    public void setUserUid(String userUid)   { this.userUid = userUid; }
    public void setName(String name)         { this.name = name; }
    public void setType(String type)         { this.type = type; }
    public void setBalance(BigDecimal b)     { this.balance = b; }
    public void setColor(String color)       { this.color = color; }
    public void setIcon(String icon)         { this.icon = icon; }
}
