package com.financeira.api.infrastructure.persistence.entity;

import com.financeira.api.domain.model.Investment;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "investments")
@SQLRestriction("deleted_at IS NULL")
public class InvestmentEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_uid", nullable = false)
    private String userUid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "current_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal rate;

    @Column
    private LocalDate maturity;

    @Column(name = "bank_id")
    private UUID bankId;

    @Column(name = "is_emergency_reserve", nullable = false)
    private Boolean isEmergencyReserve = false;

    @Column(length = 10)
    private String icon;

    @Column(length = 7)
    private String color;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public InvestmentEntity() {}

    public static InvestmentEntity fromDomain(Investment inv) {
        InvestmentEntity e = new InvestmentEntity();
        e.id = inv.getId();
        e.userUid = inv.getUserUid();
        e.name = inv.getName();
        e.type = inv.getType();
        e.amount = inv.getAmount();
        e.currentValue = inv.getCurrentValue();
        e.rate = inv.getRate();
        e.maturity = inv.getMaturity();
        e.bankId = inv.getBankId();
        e.isEmergencyReserve = inv.getIsEmergencyReserve() != null ? inv.getIsEmergencyReserve() : false;
        e.icon = inv.getIcon();
        e.color = inv.getColor();
        return e;
    }

    public Investment toDomain() {
        Investment inv = new Investment(userUid, name, type, amount, currentValue, rate,
                maturity, bankId, isEmergencyReserve, icon, color);
        inv.setId(id);
        return inv;
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
    public Instant getDeletedAt()          { return deletedAt; }

    public void setDeletedAt(Instant t)    { this.deletedAt = t; }
}
