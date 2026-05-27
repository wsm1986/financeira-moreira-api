package com.financeira.api.application.usecase.card;

import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.repository.CreditCardRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteCardUseCase {

    private final CreditCardRepository repository;

    public DeleteCardUseCase(CreditCardRepository repository) {
        this.repository = repository;
    }

    public void execute(String userUid, UUID id) {
        if (!repository.existsByIdAndUserUid(id, userUid)) {
            throw new ResourceNotFoundException("Cartão não encontrado");
        }
        repository.softDeleteByIdAndUserUid(id, userUid);
    }
}
