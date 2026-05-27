package com.financeira.api.application.usecase.bank;

import com.financeira.api.application.dto.BankResponse;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Bank;
import com.financeira.api.domain.repository.BankRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AdjustBankBalanceUseCase {

    private final BankRepository repository;

    public AdjustBankBalanceUseCase(BankRepository repository) {
        this.repository = repository;
    }

    public BankResponse execute(String userUid, UUID id, BigDecimal newBalance) {
        Bank bank = repository.findByIdAndUserUid(id, userUid)
                .orElseThrow(() -> new ResourceNotFoundException("Banco não encontrado"));
        bank.setBalance(newBalance);
        return BankResponse.from(repository.save(bank));
    }
}
