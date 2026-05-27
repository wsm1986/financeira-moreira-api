package com.financeira.api.application.usecase.recurrence;

import com.financeira.api.application.dto.RecurrenceRequest;
import com.financeira.api.application.dto.RecurrenceResponse;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Recurrence;
import com.financeira.api.domain.repository.RecurrenceRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateRecurrenceUseCase {

    private final RecurrenceRepository repository;

    public UpdateRecurrenceUseCase(RecurrenceRepository repository) {
        this.repository = repository;
    }

    public RecurrenceResponse execute(String userUid, UUID id, RecurrenceRequest request) {
        Recurrence r = repository.findByIdAndUserUid(id, userUid)
                .orElseThrow(() -> new ResourceNotFoundException("Recorrência não encontrada"));
        r.setName(request.name());
        r.setIcon(request.icon());
        r.setCategoryId(request.categoryId());
        r.setKind(request.kind());
        r.setAmount(request.amount());
        r.setCardId(request.cardId());
        r.setAccountId(request.accountId());
        r.setStartMonth(request.startMonth());
        r.setEndMonth(request.endMonth());
        r.setMonths(request.months());
        if (request.active() != null) r.setActive(request.active());
        return RecurrenceResponse.from(repository.save(r));
    }
}
