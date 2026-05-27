package com.financeira.api.application.usecase.recurrence;

import com.financeira.api.application.dto.RecurrenceResponse;
import com.financeira.api.domain.repository.RecurrenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListRecurrencesUseCase {

    private final RecurrenceRepository repository;

    public ListRecurrencesUseCase(RecurrenceRepository repository) {
        this.repository = repository;
    }

    public List<RecurrenceResponse> execute(String userUid) {
        return repository.findAllByUserUid(userUid).stream()
                .map(RecurrenceResponse::from)
                .collect(Collectors.toList());
    }
}
