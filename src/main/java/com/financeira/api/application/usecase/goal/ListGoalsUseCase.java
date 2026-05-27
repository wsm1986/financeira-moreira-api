package com.financeira.api.application.usecase.goal;

import com.financeira.api.application.dto.GoalResponse;
import com.financeira.api.domain.repository.GoalRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListGoalsUseCase {

    private final GoalRepository repository;

    public ListGoalsUseCase(GoalRepository repository) {
        this.repository = repository;
    }

    public List<GoalResponse> execute(String userUid) {
        return repository.findAllByUserUid(userUid).stream()
                .map(GoalResponse::from)
                .collect(Collectors.toList());
    }
}
