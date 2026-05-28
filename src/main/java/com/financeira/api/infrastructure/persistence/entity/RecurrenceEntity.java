package com.financeira.api.infrastructure.persistence.entity;

import com.financeira.api.domain.model.Recurrence;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recurrences")
@SQLRestriction("deleted_at IS NULL")
public class RecurrenceEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_uid", nullable = false)
    private String userUid;

    @Column(nullable = false)
    private String name;

    @Column(length = 10)
    private String icon;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(nullable = false, length = 30)
    private String kind;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "card_id")
    private UUID cardId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "start_month", nullable = false, length = 7)
    private String startMonth;

    @Column(name = "end_month", length = 7)
    private String endMonth;

    @Column(nullable = false)
    private Integer months;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public RecurrenceEntity() {}

    public static RecurrenceEntity fromDomain(Recurrence r) {
        RecurrenceEntity e = new RecurrenceEntity();
        e.id = r.getId();
        e.userUid = r.getUserUid();
        e.name = r.getName();
        e.icon = r.getIcon();
        e.categoryId = r.getCategoryId();
        e.kind = r.getKind();
        e.amount = r.getAmount();
        e.cardId = r.getCardId();
        e.accountId = r.getAccountId();
        e.startMonth = r.getStartMonth();
        e.endMonth = r.getEndMonth();
        e.months = r.getMonths();
        e.active = r.getActive();
        return e;
    }

    public Recurrence toDomain() {
        Recurrence r = new Recurrence(userUid, name, icon, categoryId, kind,
                amount, cardId, accountId, startMonth, endMonth, months, active);
        r.setId(id);
        return r;
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
    public Instant getDeletedAt() { return deletedAt; }

    public void setDeletedAt(Instant t) { this.deletedAt = t; }
}
