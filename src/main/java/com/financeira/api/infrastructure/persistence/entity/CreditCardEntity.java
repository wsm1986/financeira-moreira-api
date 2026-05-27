package com.financeira.api.infrastructure.persistence.entity;

import com.financeira.api.domain.model.CreditCard;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "credit_cards")
@SQLRestriction("deleted_at IS NULL")
public class CreditCardEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_uid", nullable = false)
    private String userUid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String brand;

    @Column(name = "last_digits", nullable = false, length = 4)
    private String lastDigits;

    @Column(name = "card_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal cardLimit;

    @Column(name = "closing_day", nullable = false)
    private Integer closingDay;

    @Column(name = "due_day", nullable = false)
    private Integer dueDay;

    @Column
    private String color;

    @Column
    private String icon;

    @Column(name = "bank_id")
    private UUID bankId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public CreditCardEntity() {}

    public static CreditCardEntity fromDomain(CreditCard c) {
        CreditCardEntity e = new CreditCardEntity();
        e.id = c.getId();
        e.userUid = c.getUserUid();
        e.name = c.getName();
        e.brand = c.getBrand();
        e.lastDigits = c.getLastDigits();
        e.cardLimit = c.getCardLimit();
        e.closingDay = c.getClosingDay();
        e.dueDay = c.getDueDay();
        e.color = c.getColor();
        e.icon = c.getIcon();
        e.bankId = c.getBankId();
        return e;
    }

    public CreditCard toDomain() {
        CreditCard c = new CreditCard(userUid, name, brand, lastDigits, cardLimit, closingDay, dueDay, color, icon, bankId);
        c.setId(id);
        return c;
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
    public Instant getDeletedAt()    { return deletedAt; }

    public void setId(UUID id)                   { this.id = id; }
    public void setUserUid(String u)             { this.userUid = u; }
    public void setName(String name)             { this.name = name; }
    public void setBrand(String brand)           { this.brand = brand; }
    public void setLastDigits(String d)          { this.lastDigits = d; }
    public void setCardLimit(BigDecimal l)       { this.cardLimit = l; }
    public void setClosingDay(Integer d)         { this.closingDay = d; }
    public void setDueDay(Integer d)             { this.dueDay = d; }
    public void setColor(String color)           { this.color = color; }
    public void setIcon(String icon)             { this.icon = icon; }
    public void setBankId(UUID bankId)           { this.bankId = bankId; }
    public void setDeletedAt(Instant t)          { this.deletedAt = t; }
}
