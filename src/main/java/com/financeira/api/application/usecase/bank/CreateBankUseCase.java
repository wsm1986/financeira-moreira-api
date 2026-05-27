package com.financeira.api.application.usecase.bank;

import com.financeira.api.application.dto.BankRequest;
import com.financeira.api.application.dto.BankResponse;
import com.financeira.api.domain.model.Bank;
import com.financeira.api.domain.repository.BankRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateBankUseCase {

    private final BankRepository repository;

    public CreateBankUseCase(BankRepository repository) {
        this.repository = repository;
    }

    public BankResponse execute(String userUid, BankRequest request) {
        Bank bank = new Bank(userUid, request.name(), request.type(),
                request.balance(), request.color(), request.icon());
        return BankResponse.from(repository.save(bank));
    }
}
