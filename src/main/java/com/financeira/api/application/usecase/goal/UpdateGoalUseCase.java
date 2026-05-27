package com.financeira.api.application.usecase.goal;

import com.financeira.api.application.dto.GoalRequest;
import com.financeira.api.application.dto.GoalResponse;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Goal;
import com.financeira.api.domain.repository.GoalRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateGoalUseCase {

    private final GoalRepository repository;

    public UpdateGoalUseCase(GoalRepository repository) {
        this.repository = repository;
    }

    public GoalResponse execute(String userUid, UUID id, GoalRequest request) {
        Goal goal = repository.findByIdAndUserUid(id, userUid)
                .orElseThrow(() -> new ResourceNotFoundException("Meta não encontrada"));
        goal.setName(request.name());
        goal.setIcon(request.icon());
        goal.setTargetAmount(request.targetAmount());
        if (request.currentAmount() != null) goal.setCurrentAmount(request.currentAmount());
        goal.setDeadline(request.deadline());
        goal.setColor(request.color());
        if (request.status() != null) goal.setStatus(request.status());
        goal.setNotes(request.notes());
        return GoalResponse.from(repository.save(goal));
    }
}
