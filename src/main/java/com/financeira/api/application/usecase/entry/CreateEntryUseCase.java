package com.financeira.api.application.usecase.entry;

import com.financeira.api.application.dto.EntryRequest;
import com.financeira.api.application.dto.EntryResponse;
import com.financeira.api.domain.model.Entry;
import com.financeira.api.domain.repository.EntryRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateEntryUseCase {

    private final EntryRepository repository;

    public CreateEntryUseCase(EntryRepository repository) {
        this.repository = repository;
    }

    public EntryResponse execute(String userUid, EntryRequest request) {
        Entry entry = new Entry(userUid, request.monthKey(), request.kind(), request.name(),
                request.categoryId(), request.amount(), request.entryDate(), request.icon());
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
        entry.setIsPaid(request.isPaid() != null ? request.isPaid() : false);
        entry.setIsReconciled(request.isReconciled() != null ? request.isReconciled() : false);
        entry.setNotes(request.notes());
        entry.setTags(request.tags());
        return EntryResponse.from(repository.save(entry));
    }
}
