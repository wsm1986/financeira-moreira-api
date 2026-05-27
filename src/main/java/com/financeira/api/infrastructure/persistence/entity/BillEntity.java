package com.financeira.api.infrastructure.persistence.entity;

import com.financeira.api.domain.model.Bill;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bills")
@SQLRestriction("deleted_at IS NULL")
public class BillEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_uid", nullable = false)
    private String userUid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(nullable = false)
    private Boolean paid = false;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "bank_id")
    private UUID bankId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, length = 20)
    private String type;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public BillEntity() {}

    public static BillEntity fromDomain(Bill b) {
        BillEntity e = new BillEntity();
        e.id = b.getId();
        e.userUid = b.getUserUid();
        e.name = b.getName();
        e.amount = b.getAmount();
        e.dueDate = b.getDueDate();
        e.categoryId = b.getCategoryId();
        e.paid = b.getPaid() != null ? b.getPaid() : false;
        e.paidDate = b.getPaidDate();
        e.bankId = b.getBankId();
        e.notes = b.getNotes();
        e.type = b.getType();
        return e;
    }

    public Bill toDomain() {
        Bill b = new Bill(userUid, name, amount, dueDate, categoryId, paid, paidDate, bankId, notes, type);
        b.setId(id);
        return b;
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
    public Instant getDeletedAt() { return deletedAt; }

    public void setDeletedAt(Instant t) { this.deletedAt = t; }
}
