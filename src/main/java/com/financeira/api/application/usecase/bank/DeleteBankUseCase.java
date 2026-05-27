package com.financeira.api.application.usecase.bank;

import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.repository.BankRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteBankUseCase {

    private final BankRepository repository;

    public DeleteBankUseCase(BankRepository repository) {
        this.repository = repository;
    }

    public void execute(String userUid, UUID id) {
        if (!repository.existsByIdAndUserUid(id, userUid)) {
            throw new ResourceNotFoundException("Banco não encontrado");
        }
        repository.softDeleteByIdAndUserUid(id, userUid);
    }
}
