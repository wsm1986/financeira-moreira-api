package com.financeira.api.application.usecase.goal;

import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.repository.GoalRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteGoalUseCase {

    private final GoalRepository repository;

    public DeleteGoalUseCase(GoalRepository repository) {
        this.repository = repository;
    }

    public void execute(String userUid, UUID id) {
        if (!repository.existsByIdAndUserUid(id, userUid)) {
            throw new ResourceNotFoundException("Meta não encontrada");
        }
        repository.softDeleteByIdAndUserUid(id, userUid);
    }
}
