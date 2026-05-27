package com.financeira.api.application.usecase.recurrence;

import com.financeira.api.application.dto.RecurrenceRequest;
import com.financeira.api.application.dto.RecurrenceResponse;
import com.financeira.api.domain.model.Recurrence;
import com.financeira.api.domain.repository.RecurrenceRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateRecurrenceUseCase {

    private final RecurrenceRepository repository;

    public CreateRecurrenceUseCase(RecurrenceRepository repository) {
        this.repository = repository;
    }

    public RecurrenceResponse execute(String userUid, RecurrenceRequest request) {
        Recurrence r = new Recurrence(userUid, request.name(), request.icon(), request.categoryId(),
                request.kind(), request.amount(), request.cardId(), request.accountId(),
                request.startMonth(), request.endMonth(), request.months(), request.active());
        return RecurrenceResponse.from(repository.save(r));
    }
}
