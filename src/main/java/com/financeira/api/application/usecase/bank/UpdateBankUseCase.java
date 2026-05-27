package com.financeira.api.application.usecase.bank;

import com.financeira.api.application.dto.BankRequest;
import com.financeira.api.application.dto.BankResponse;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Bank;
import com.financeira.api.domain.repository.BankRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateBankUseCase {

    private final BankRepository repository;

    public UpdateBankUseCase(BankRepository repository) {
        this.repository = repository;
    }

    public BankResponse execute(String userUid, UUID id, BankRequest request) {
        Bank bank = repository.findByIdAndUserUid(id, userUid)
                .orElseThrow(() -> new ResourceNotFoundException("Banco não encontrado"));
        bank.setName(request.name());
        bank.setType(request.type());
        if (request.balance() != null) bank.setBalance(request.balance());
        bank.setColor(request.color());
        bank.setIcon(request.icon());
        return BankResponse.from(repository.save(bank));
    }
}
