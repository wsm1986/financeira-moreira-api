package com.financeira.api.infrastructure.persistence.entity;

import com.financeira.api.domain.model.Entry;
import com.financeira.api.infrastructure.persistence.converter.TagsConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "entries")
@SQLRestriction("deleted_at IS NULL")
public class EntryEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_uid", nullable = false)
    private String userUid;

    @Column(name = "month_key", nullable = false, length = 7)
    private String monthKey;

    @Column(nullable = false, length = 30)
    private String kind;

    @Column(nullable = false)
    private String name;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(length = 10)
    private String icon;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "installment_total")
    private Integer installmentTotal;

    @Column(name = "installment_current")
    private Integer installmentCurrent;

    @Column(name = "installment_group_id")
    private UUID installmentGroupId;

    @Column(name = "recurrence_id")
    private UUID recurrenceId;

    @Column(name = "recurrence_months")
    private Integer recurrenceMonths;

    @Column(name = "card_id")
    private UUID cardId;

    @Column(name = "billing_month", length = 7)
    private String billingMonth;

    @Column(name = "invoice_ref", length = 7)
    private String invoiceRef;

    @Column(name = "to_account_id")
    private UUID toAccountId;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "is_reconciled", nullable = false)
    private Boolean isReconciled = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Convert(converter = TagsConverter.class)
    @Column(name = "tags", columnDefinition = "TEXT")
    private List<String> tags;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public EntryEntity() {}

    public static EntryEntity fromDomain(Entry e) {
        EntryEntity entity = new EntryEntity();
        entity.id = e.getId();
        entity.userUid = e.getUserUid();
        entity.monthKey = e.getMonthKey();
        entity.kind = e.getKind();
        entity.name = e.getName();
        entity.categoryId = e.getCategoryId();
        entity.amount = e.getAmount();
        entity.entryDate = e.getEntryDate();
        entity.icon = e.getIcon();
        entity.accountId = e.getAccountId();
        entity.installmentTotal = e.getInstallmentTotal();
        entity.installmentCurrent = e.getInstallmentCurrent();
        entity.installmentGroupId = e.getInstallmentGroupId();
        entity.recurrenceId = e.getRecurrenceId();
        entity.recurrenceMonths = e.getRecurrenceMonths();
        entity.cardId = e.getCardId();
        entity.billingMonth = e.getBillingMonth();
        entity.invoiceRef = e.getInvoiceRef();
        entity.toAccountId = e.getToAccountId();
        entity.isPaid = e.getIsPaid() != null ? e.getIsPaid() : false;
        entity.isReconciled = e.getIsReconciled() != null ? e.getIsReconciled() : false;
        entity.notes = e.getNotes();
        entity.tags = e.getTags();
        return entity;
    }

    public Entry toDomain() {
        Entry e = new Entry(userUid, monthKey, kind, name, categoryId, amount, entryDate, icon);
        e.setId(id);
        e.setAccountId(accountId);
        e.setInstallmentTotal(installmentTotal);
        e.setInstallmentCurrent(installmentCurrent);
        e.setInstallmentGroupId(installmentGroupId);
        e.setRecurrenceId(recurrenceId);
        e.setRecurrenceMonths(recurrenceMonths);
        e.setCardId(cardId);
        e.setBillingMonth(billingMonth);
        e.setInvoiceRef(invoiceRef);
        e.setToAccountId(toAccountId);
        e.setIsPaid(isPaid);
        e.setIsReconciled(isReconciled);
        e.setNotes(notes);
        e.setTags(tags);
        return e;
    }

    public UUID getId()                    { return id; }
    public String getUserUid()             { return userUid; }
    public String getMonthKey()            { return monthKey; }
    public String getKind()                { return kind; }
    public String getName()                { return name; }
    public UUID getCategoryId()            { return categoryId; }
    public BigDecimal getAmount()          { return amount; }
    public LocalDate getEntryDate()        { return entryDate; }
    public String getIcon()                { return icon; }
    public UUID getAccountId()             { return accountId; }
    public Integer getInstallmentTotal()   { return installmentTotal; }
    public Integer getInstallmentCurrent() { return installmentCurrent; }
    public UUID getInstallmentGroupId()    { return installmentGroupId; }
    public UUID getRecurrenceId()          { return recurrenceId; }
    public Integer getRecurrenceMonths()   { return recurrenceMonths; }
    public UUID getCardId()                { return cardId; }
    public String getBillingMonth()        { return billingMonth; }
    public String getInvoiceRef()          { return invoiceRef; }
    public UUID getToAccountId()           { return toAccountId; }
    public Boolean getIsPaid()             { return isPaid; }
    public Boolean getIsReconciled()       { return isReconciled; }
    public String getNotes()               { return notes; }
    public List<String> getTags()          { return tags; }
    public Instant getDeletedAt()          { return deletedAt; }

    public void setDeletedAt(Instant t)    { this.deletedAt = t; }
}
