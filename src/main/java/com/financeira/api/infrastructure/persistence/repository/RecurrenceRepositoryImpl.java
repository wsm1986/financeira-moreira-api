package com.financeira.api.infrastructure.persistence.repository;

import com.financeira.api.domain.model.Recurrence;
import com.financeira.api.domain.repository.RecurrenceRepository;
import com.financeira.api.infrastructure.persistence.entity.RecurrenceEntity;
import com.financeira.api.infrastructure.persistence.jpa.RecurrenceJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class RecurrenceRepositoryImpl implements RecurrenceRepository {

    private final RecurrenceJpaRepository jpa;

    public RecurrenceRepositoryImpl(RecurrenceJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Recurrence save(Recurrence r) {
        return jpa.save(RecurrenceEntity.fromDomain(r)).toDomain();
    }

    @Override
    public Optional<Recurrence> findByIdAndUserUid(UUID id, String uid) {
        return jpa.findByIdAndUserUid(id, uid).map(RecurrenceEntity::toDomain);
    }

    @Override
    public List<Recurrence> findAllByUserUid(String uid) {
        return jpa.findAllByUserUid(uid).stream()
                .map(RecurrenceEntity::toDomain)
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
