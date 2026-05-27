package com.financeira.api.application.usecase.goal;

import com.financeira.api.application.dto.GoalResponse;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Goal;
import com.financeira.api.domain.repository.GoalRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AddGoalProgressUseCase {

    private final GoalRepository repository;

    public AddGoalProgressUseCase(GoalRepository repository) {
        this.repository = repository;
    }

    public GoalResponse execute(String userUid, UUID id, BigDecimal amount) {
        Goal goal = repository.findByIdAndUserUid(id, userUid)
                .orElseThrow(() -> new ResourceNotFoundException("Meta não encontrada"));
        BigDecimal current = goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO;
        goal.setCurrentAmount(current.add(amount));
        return GoalResponse.from(repository.save(goal));
    }
}
