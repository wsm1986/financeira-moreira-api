package com.financeira.api.application.usecase.entry;

import com.financeira.api.application.dto.EntryRequest;
import com.financeira.api.application.dto.EntryResponse;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Entry;
import com.financeira.api.domain.repository.EntryRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateEntryUseCase {

    private final EntryRepository repository;

    public UpdateEntryUseCase(EntryRepository repository) {
        this.repository = repository;
    }

    public EntryResponse execute(String userUid, UUID id, EntryRequest request) {
        Entry entry = repository.findByIdAndUserUid(id, userUid)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento não encontrado"));
        entry.setMonthKey(request.monthKey());
        entry.setKind(request.kind());
        entry.setName(request.name());
        entry.setCategoryId(request.categoryId());
        entry.setAmount(request.amount());
        entry.setEntryDate(request.entryDate());
        entry.setIcon(request.icon());
        entry.setAccountId(request.accountId());
        entry.setInstallmentTotal(request.installmentTotal());
        entry.setInstallmentCurrent(request.installmentCurrent());
        entry.setInstallmentGroupId(request.installmentGroupId());
        entry.setRecurrenceId(request.recurrenceId());
        entry.setRecurrenceMonths(request.recurrenceMonths());
        entry.setCardId(request.cardId());
        entry.setBillingMonth(request.billingMonth());
        entry.setInvoiceRef(request.invoiceRef());
        entry.setToAccountId(request.toAccountId());
        if (request.isPaid() != null) entry.setIsPaid(request.isPaid());
        if (request.isReconciled() != null) entry.setIsReconciled(request.isReconciled());
        entry.setNotes(request.notes());
        entry.setTags(request.tags());
        return EntryResponse.from(repository.save(entry));
    }
}
