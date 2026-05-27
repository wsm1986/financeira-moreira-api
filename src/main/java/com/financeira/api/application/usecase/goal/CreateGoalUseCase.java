package com.financeira.api.application.usecase.goal;

import com.financeira.api.application.dto.GoalRequest;
import com.financeira.api.application.dto.GoalResponse;
import com.financeira.api.domain.model.Goal;
import com.financeira.api.domain.repository.GoalRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateGoalUseCase {

    private final GoalRepository repository;

    public CreateGoalUseCase(GoalRepository repository) {
        this.repository = repository;
    }

    public GoalResponse execute(String userUid, GoalRequest request) {
        Goal goal = new Goal(userUid, request.name(), request.icon(), request.targetAmount(),
                request.currentAmount(), request.deadline(), request.color(),
                request.status(), request.notes());
        return GoalResponse.from(repository.save(goal));
    }
}
