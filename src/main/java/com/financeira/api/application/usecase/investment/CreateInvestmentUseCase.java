package com.financeira.api.application.usecase.investment;

import com.financeira.api.application.dto.InvestmentRequest;
import com.financeira.api.application.dto.InvestmentResponse;
import com.financeira.api.domain.model.Investment;
import com.financeira.api.domain.repository.InvestmentRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateInvestmentUseCase {

    private final InvestmentRepository repository;

    public CreateInvestmentUseCase(InvestmentRepository repository) {
        this.repository = repository;
    }

    public InvestmentResponse execute(String userUid, InvestmentRequest request) {
        Investment inv = new Investment(userUid, request.name(), request.type(),
                request.amount(), request.currentValue(), request.rate(),
                request.maturity(), request.bankId(), request.isEmergencyReserve(),
                request.icon(), request.color());
        return InvestmentResponse.from(repository.save(inv));
    }
}
