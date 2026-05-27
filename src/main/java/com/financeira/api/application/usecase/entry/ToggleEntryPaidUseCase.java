package com.financeira.api.application.usecase.entry;

import com.financeira.api.application.dto.EntryResponse;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Entry;
import com.financeira.api.domain.repository.EntryRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ToggleEntryPaidUseCase {

    private final EntryRepository repository;

    public ToggleEntryPaidUseCase(EntryRepository repository) {
        this.repository = repository;
    }

    public EntryResponse execute(String userUid, UUID id) {
        Entry entry = repository.findByIdAndUserUid(id, userUid)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento não encontrado"));
        entry.setIsPaid(!Boolean.TRUE.equals(entry.getIsPaid()));
        return EntryResponse.from(repository.save(entry));
    }
}
