package com.financeira.api.application.usecase.card;

import com.financeira.api.application.dto.CardRequest;
import com.financeira.api.application.dto.CardResponse;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.CreditCard;
import com.financeira.api.domain.repository.CreditCardRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateCardUseCase {

    private final CreditCardRepository repository;

    public UpdateCardUseCase(CreditCardRepository repository) {
        this.repository = repository;
    }

    public CardResponse execute(String userUid, UUID id, CardRequest request) {
        CreditCard card = repository.findByIdAndUserUid(id, userUid)
                .orElseThrow(() -> new ResourceNotFoundException("Cartão não encontrado"));
        card.setName(request.name());
        card.setBrand(request.brand());
        card.setLastDigits(request.lastDigits());
        card.setCardLimit(request.cardLimit());
        card.setClosingDay(request.closingDay());
        card.setDueDay(request.dueDay());
        card.setColor(request.color());
        card.setIcon(request.icon());
        card.setBankId(request.bankId());
        return CardResponse.from(repository.save(card));
    }
}
