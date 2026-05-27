package com.financeira.api.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Goal {

    private UUID id;
    private String userUid;
    private String name;
    private String icon;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private String deadline;
    private String color;
    private String status;
    private String notes;

    public Goal() {}

    public Goal(String userUid, String name, String icon, BigDecimal targetAmount,
                BigDecimal currentAmount, String deadline, String color, String status, String notes) {
        this.id = UUID.randomUUID();
        this.userUid = userUid;
        this.name = name;
        this.icon = icon;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount != null ? currentAmount : BigDecimal.ZERO;
        this.deadline = deadline;
        this.color = color;
        this.status = status != null ? status : "on-track";
        this.notes = notes;
    }

    public UUID getId()                  { return id; }
    public String getUserUid()           { return userUid; }
    public String getName()              { return name; }
    public String getIcon()              { return icon; }
    public BigDecimal getTargetAmount()  { return targetAmount; }
    public BigDecimal getCurrentAmount() { return currentAmount; }
    public String getDeadline()          { return deadline; }
    public String getColor()             { return color; }
    public String getStatus()            { return status; }
    public String getNotes()             { return notes; }

    public void setId(UUID id)                      { this.id = id; }
    public void setUserUid(String u)                { this.userUid = u; }
    public void setName(String n)                   { this.name = n; }
    public void setIcon(String i)                   { this.icon = i; }
    public void setTargetAmount(BigDecimal t)        { this.targetAmount = t; }
    public void setCurrentAmount(BigDecimal c)       { this.currentAmount = c; }
    public void setDeadline(String d)               { this.deadline = d; }
    public void setColor(String c)                  { this.color = c; }
    public void setStatus(String s)                 { this.status = s; }
    public void setNotes(String n)                  { this.notes = n; }
}
