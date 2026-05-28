package com.financeira.api.application.usecase.investment;

import com.financeira.api.application.dto.InvestmentRequest;
import com.financeira.api.application.dto.InvestmentResponse;
import com.financeira.api.domain.model.Investment;
import com.financeira.api.domain.repository.InvestmentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CreateInvestmentUseCase {

    private final InvestmentRepository repository;

    public CreateInvestmentUseCase(InvestmentRepository repository) {
        this.repository = repository;
    }

    public InvestmentResponse execute(String userUid, InvestmentRequest request) {
        // portal pode mandar "returns" (BigDecimal) ou null; default = amount
        BigDecimal currentValue = request.currentValue() != null
                ? request.currentValue()
                : request.amount();
        // portal manda rate como String ("CDI 120%"), tenta extrair número ou null
        BigDecimal rate = parseRate(request.rateStr());

        Investment inv = new Investment(userUid, request.name(), request.type(),
                request.amount(), currentValue, rate,
                request.maturity(), request.bankId(), request.isEmergencyReserve(),
                request.icon(), request.color());
        return InvestmentResponse.from(repository.save(inv));
    }

    private BigDecimal parseRate(String rateStr) {
        if (rateStr == null || rateStr.isBlank()) return null;
        String cleaned = rateStr.replaceAll("[^0-9.,]", "").replace(",", ".");
        if (cleaned.isEmpty()) return null;
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
