package com.financeira.api.domain.repository;

import com.financeira.api.domain.model.Recurrence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurrenceRepository {
    Recurrence save(Recurrence r);
    Optional<Recurrence> findByIdAndUserUid(UUID id, String uid);
    List<Recurrence> findAllByUserUid(String uid);
    void softDeleteByIdAndUserUid(UUID id, String uid);
    boolean existsByIdAndUserUid(UUID id, String uid);
}
