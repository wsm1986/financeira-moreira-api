package com.financeira.api.domain.repository;

import com.financeira.api.domain.model.Entry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntryRepository {
    Entry save(Entry e);
    Optional<Entry> findByIdAndUserUid(UUID id, String uid);
    List<Entry> findAllByUserUidAndMonthKey(String uid, String monthKey);
    List<Entry> findAllByUserUidAndYear(String uid, int year);
    void softDeleteByIdAndUserUid(UUID id, String uid);
    boolean existsByIdAndUserUid(UUID id, String uid);
}
