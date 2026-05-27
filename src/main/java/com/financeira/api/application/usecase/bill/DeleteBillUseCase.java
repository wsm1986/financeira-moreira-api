package com.financeira.api.application.usecase.bill;

import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteBillUseCase {

    private final BillRepository repository;

    public DeleteBillUseCase(BillRepository repository) {
        this.repository = repository;
    }

    public void execute(String userUid, UUID id) {
        if (!repository.existsByIdAndUserUid(id, userUid)) {
            throw new ResourceNotFoundException("Conta não encontrada");
        }
        repository.softDeleteByIdAndUserUid(id, userUid);
    }
}
