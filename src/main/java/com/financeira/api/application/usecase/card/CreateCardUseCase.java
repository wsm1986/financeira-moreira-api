package com.financeira.api.application.usecase.card;

import com.financeira.api.application.dto.CardRequest;
import com.financeira.api.application.dto.CardResponse;
import com.financeira.api.domain.model.CreditCard;
import com.financeira.api.domain.repository.CreditCardRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateCardUseCase {

    private final CreditCardRepository repository;

    public CreateCardUseCase(CreditCardRepository repository) {
        this.repository = repository;
    }

    public CardResponse execute(String userUid, CardRequest request) {
        CreditCard card = new CreditCard(userUid, request.name(), request.brand(), request.lastDigits(),
                request.cardLimit(), request.closingDay(), request.dueDay(),
                request.color(), request.icon(), request.bankId());
        return CardResponse.from(repository.save(card));
    }
}
