package com.financeira.api.application.usecase.bill;

import com.financeira.api.application.dto.BillRequest;
import com.financeira.api.application.dto.BillResponse;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Bill;
import com.financeira.api.domain.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateBillUseCase {

    private final BillRepository repository;

    public UpdateBillUseCase(BillRepository repository) {
        this.repository = repository;
    }

    public BillResponse execute(String userUid, UUID id, BillRequest request) {
        Bill bill = repository.findByIdAndUserUid(id, userUid)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));
        bill.setName(request.name());
        bill.setAmount(request.amount());
        bill.setDueDate(request.dueDate());
        bill.setCategoryId(request.categoryId());
        if (request.paid() != null) bill.setPaid(request.paid());
        bill.setPaidDate(request.paidDate());
        bill.setBankId(request.bankId());
        bill.setNotes(request.notes());
        if (request.type() != null) bill.setType(request.type());
        return BillResponse.from(repository.save(bill));
    }
}
