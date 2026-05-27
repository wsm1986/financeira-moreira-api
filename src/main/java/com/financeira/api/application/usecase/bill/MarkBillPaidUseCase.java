package com.financeira.api.application.usecase.bill;

import com.financeira.api.application.dto.BillResponse;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Bill;
import com.financeira.api.domain.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class MarkBillPaidUseCase {

    private final BillRepository repository;

    public MarkBillPaidUseCase(BillRepository repository) {
        this.repository = repository;
    }

    public BillResponse execute(String userUid, UUID id) {
        Bill bill = repository.findByIdAndUserUid(id, userUid)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));
        bill.setPaid(true);
        bill.setPaidDate(LocalDate.now());
        return BillResponse.from(repository.save(bill));
    }
}
