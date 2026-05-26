package com.financeira.api.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Category {

    private UUID id;
    private String userUid;
    private String name;
    private String icon;
    private BigDecimal budget;
    private String color;
    private String type;   // expense | income | both
    private String nature; // essencial | desejo | investimento | null

    public Category() {}

    public Category(String userUid, String name, String icon, BigDecimal budget,
                    String color, String type, String nature) {
        this.id = UUID.randomUUID();
        this.userUid = userUid;
        this.name = name;
        this.icon = icon;
        this.budget = budget != null ? budget : BigDecimal.ZERO;
        this.color = color;
        this.type = type;
        this.nature = nature;
    }

    public UUID getId()        { return id; }
    public String getUserUid() { return userUid; }
    public String getName()    { return name; }
    public String getIcon()    { return icon; }
    public BigDecimal getBudget() { return budget; }
    public String getColor()   { return color; }
    public String getType()    { return type; }
    public String getNature()  { return nature; }

    public void setId(UUID id)           { this.id = id; }
    public void setUserUid(String uid)   { this.userUid = uid; }
    public void setName(String name)     { this.name = name; }
    public void setIcon(String icon)     { this.icon = icon; }
    public void setBudget(BigDecimal b)  { this.budget = b; }
    public void setColor(String color)   { this.color = color; }
    public void setType(String type)     { this.type = type; }
    public void setNature(String nature) { this.nature = nature; }
}
