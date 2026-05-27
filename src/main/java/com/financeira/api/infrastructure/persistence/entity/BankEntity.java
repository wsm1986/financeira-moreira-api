package com.financeira.api.infrastructure.persistence.entity;

import com.financeira.api.domain.model.Bank;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "banks")
@SQLRestriction("deleted_at IS NULL")
public class BankEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_uid", nullable = false)
    private String userUid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance;

    @Column
    private String color;

    @Column
    private String icon;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public BankEntity() {}

    public static BankEntity fromDomain(Bank b) {
        BankEntity e = new BankEntity();
        e.id = b.getId();
        e.userUid = b.getUserUid();
        e.name = b.getName();
        e.type = b.getType();
        e.balance = b.getBalance();
        e.color = b.getColor();
        e.icon = b.getIcon();
        return e;
    }

    public Bank toDomain() {
        Bank b = new Bank(userUid, name, type, balance, color, icon);
        b.setId(id);
        return b;
    }

    public UUID getId()            { return id; }
    public String getUserUid()     { return userUid; }
    public String getName()        { return name; }
    public String getType()        { return type; }
    public BigDecimal getBalance() { return balance; }
    public String getColor()       { return color; }
    public String getIcon()        { return icon; }
    public Instant getDeletedAt()  { return deletedAt; }

    public void setId(UUID id)               { this.id = id; }
    public void setUserUid(String userUid)   { this.userUid = userUid; }
    public void setName(String name)         { this.name = name; }
    public void setType(String type)         { this.type = type; }
    public void setBalance(BigDecimal b)     { this.balance = b; }
    public void setColor(String color)       { this.color = color; }
    public void setIcon(String icon)         { this.icon = icon; }
    public void setDeletedAt(Instant t)      { this.deletedAt = t; }
}
