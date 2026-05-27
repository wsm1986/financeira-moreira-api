package com.financeira.api.application.usecase.bill;

import com.financeira.api.application.dto.BillRequest;
import com.financeira.api.application.dto.BillResponse;
import com.financeira.api.domain.model.Bill;
import com.financeira.api.domain.repository.BillRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateBillUseCase {

    private final BillRepository repository;

    public CreateBillUseCase(BillRepository repository) {
        this.repository = repository;
    }

    public BillResponse execute(String userUid, BillRequest request) {
        Bill bill = new Bill(userUid, request.name(), request.amount(), request.dueDate(),
                request.categoryId(), request.paid(), request.paidDate(),
                request.bankId(), request.notes(),
                request.type() != null ? request.type() : "pagar");
        return BillResponse.from(repository.save(bill));
    }
}
