package com.financeira.api.application.usecase.investment;

import com.financeira.api.application.dto.InvestmentRequest;
import com.financeira.api.application.dto.InvestmentResponse;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Investment;
import com.financeira.api.domain.repository.InvestmentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class UpdateInvestmentUseCase {

    private final InvestmentRepository repository;

    public UpdateInvestmentUseCase(InvestmentRepository repository) {
        this.repository = repository;
    }

    public InvestmentResponse execute(String userUid, UUID id, InvestmentRequest request) {
        Investment inv = repository.findByIdAndUserUid(id, userUid)
                .orElseThrow(() -> new ResourceNotFoundException("Investimento não encontrado"));
        inv.setName(request.name());
        inv.setType(request.type());
        inv.setAmount(request.amount());
        // portal pode mandar "returns" null; default = amount
        inv.setCurrentValue(request.currentValue() != null ? request.currentValue() : request.amount());
        // portal manda rate como String ("CDI 120%"), tenta extrair número ou null
        inv.setRate(parseRate(request.rateStr()));
        inv.setMaturity(request.maturity());
        inv.setBankId(request.bankId());
        if (request.isEmergencyReserve() != null) inv.setIsEmergencyReserve(request.isEmergencyReserve());
        inv.setIcon(request.icon());
        inv.setColor(request.color());
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
