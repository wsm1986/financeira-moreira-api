package com.financeira.api.application.usecase.entry;

import com.financeira.api.application.dto.EntryResponse;
import com.financeira.api.domain.repository.EntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListEntriesByMonthUseCase {

    private final EntryRepository repository;

    public ListEntriesByMonthUseCase(EntryRepository repository) {
        this.repository = repository;
    }

    public List<EntryResponse> execute(String userUid, String monthKey) {
        return repository.findAllByUserUidAndMonthKey(userUid, monthKey).stream()
                .map(EntryResponse::from)
                .collect(Collectors.toList());
    }
}
