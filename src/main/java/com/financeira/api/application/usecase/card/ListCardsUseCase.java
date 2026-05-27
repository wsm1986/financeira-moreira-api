package com.financeira.api.application.usecase.card;

import com.financeira.api.application.dto.CardResponse;
import com.financeira.api.domain.repository.CreditCardRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListCardsUseCase {

    private final CreditCardRepository repository;

    public ListCardsUseCase(CreditCardRepository repository) {
        this.repository = repository;
    }

    public List<CardResponse> execute(String userUid) {
        return repository.findAllByUserUid(userUid).stream()
                .map(CardResponse::from)
                .collect(Collectors.toList());
    }
}
