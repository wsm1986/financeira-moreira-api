package com.financeira.api.application.usecase.recurrence;

import com.financeira.api.application.dto.RecurrenceResponse;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Recurrence;
import com.financeira.api.domain.repository.RecurrenceRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CancelRecurrenceUseCase {

    private final RecurrenceRepository repository;

    public CancelRecurrenceUseCase(RecurrenceRepository repository) {
        this.repository = repository;
    }

    public RecurrenceResponse execute(String userUid, UUID id) {
        Recurrence r = repository.findByIdAndUserUid(id, userUid)
                .orElseThrow(() -> new ResourceNotFoundException("Recorrência não encontrada"));
        r.setActive(false);
        return RecurrenceResponse.from(repository.save(r));
    }
}
