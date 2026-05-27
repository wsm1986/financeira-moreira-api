package com.financeira.api.infrastructure.persistence.repository;

import com.financeira.api.domain.model.Entry;
import com.financeira.api.domain.repository.EntryRepository;
import com.financeira.api.infrastructure.persistence.entity.EntryEntity;
import com.financeira.api.infrastructure.persistence.jpa.EntryJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class EntryRepositoryImpl implements EntryRepository {

    private final EntryJpaRepository jpa;

    public EntryRepositoryImpl(EntryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Entry save(Entry e) {
        return jpa.save(EntryEntity.fromDomain(e)).toDomain();
    }

    @Override
    public Optional<Entry> findByIdAndUserUid(UUID id, String uid) {
        return jpa.findByIdAndUserUid(id, uid).map(EntryEntity::toDomain);
    }

    @Override
    public List<Entry> findAllByUserUidAndMonthKey(String uid, String monthKey) {
        return jpa.findAllByUserUidAndMonthKey(uid, monthKey).stream()
                .map(EntryEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void softDeleteByIdAndUserUid(UUID id, String uid) {
        jpa.softDeleteByIdAndUserUid(id, uid, Instant.now());
    }

    @Override
    public boolean existsByIdAndUserUid(UUID id, String uid) {
        return jpa.existsByIdAndUserUid(id, uid);
    }
}
