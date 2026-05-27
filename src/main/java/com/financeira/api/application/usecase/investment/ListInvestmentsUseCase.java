package com.financeira.api.application.usecase.investment;

import com.financeira.api.application.dto.InvestmentResponse;
import com.financeira.api.domain.repository.InvestmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListInvestmentsUseCase {

    private final InvestmentRepository repository;

    public ListInvestmentsUseCase(InvestmentRepository repository) {
        this.repository = repository;
    }

    public List<InvestmentResponse> execute(String userUid) {
        return repository.findAllByUserUid(userUid).stream()
                .map(InvestmentResponse::from)
                .collect(Collectors.toList());
    }
}
