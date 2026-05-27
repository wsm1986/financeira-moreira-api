package com.financeira.api.application.usecase.recurrence;

import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.repository.RecurrenceRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteRecurrenceUseCase {

    private final RecurrenceRepository repository;

    public DeleteRecurrenceUseCase(RecurrenceRepository repository) {
        this.repository = repository;
    }

    public void execute(String userUid, UUID id) {
        if (!repository.existsByIdAndUserUid(id, userUid)) {
            throw new ResourceNotFoundException("Recorrência não encontrada");
        }
        repository.softDeleteByIdAndUserUid(id, userUid);
    }
}
