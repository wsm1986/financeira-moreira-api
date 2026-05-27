package com.financeira.api.application.usecase.investment;

import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.repository.InvestmentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteInvestmentUseCase {

    private final InvestmentRepository repository;

    public DeleteInvestmentUseCase(InvestmentRepository repository) {
        this.repository = repository;
    }

    public void execute(String userUid, UUID id) {
        if (!repository.existsByIdAndUserUid(id, userUid)) {
            throw new ResourceNotFoundException("Investimento não encontrado");
        }
        repository.softDeleteByIdAndUserUid(id, userUid);
    }
}
