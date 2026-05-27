package com.financeira.api.infrastructure.persistence.entity;

import com.financeira.api.domain.model.Goal;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "goals")
@SQLRestriction("deleted_at IS NULL")
public class GoalEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_uid", nullable = false)
    private String userUid;

    @Column(nullable = false)
    private String name;

    @Column(length = 10)
    private String icon;

    @Column(name = "target_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "current_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 7)
    private String deadline;

    @Column(length = 50)
    private String color;

    @Column(nullable = false, length = 20)
    private String status = "on-track";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public GoalEntity() {}

    public static GoalEntity fromDomain(Goal g) {
        GoalEntity e = new GoalEntity();
        e.id = g.getId();
        e.userUid = g.getUserUid();
        e.name = g.getName();
        e.icon = g.getIcon();
        e.targetAmount = g.getTargetAmount();
        e.currentAmount = g.getCurrentAmount() != null ? g.getCurrentAmount() : BigDecimal.ZERO;
        e.deadline = g.getDeadline();
        e.color = g.getColor();
        e.status = g.getStatus() != null ? g.getStatus() : "on-track";
        e.notes = g.getNotes();
        return e;
    }

    public Goal toDomain() {
        Goal g = new Goal(userUid, name, icon, targetAmount, currentAmount, deadline, color, status, notes);
        g.setId(id);
        return g;
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
    public Instant getDeletedAt()        { return deletedAt; }

    public void setDeletedAt(Instant t)  { this.deletedAt = t; }
}
