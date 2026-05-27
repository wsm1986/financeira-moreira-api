package com.financeira.api.infrastructure.persistence.repository;

import com.financeira.api.domain.model.Goal;
import com.financeira.api.domain.repository.GoalRepository;
import com.financeira.api.infrastructure.persistence.entity.GoalEntity;
import com.financeira.api.infrastructure.persistence.jpa.GoalJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class GoalRepositoryImpl implements GoalRepository {

    private final GoalJpaRepository jpa;

    public GoalRepositoryImpl(GoalJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Goal save(Goal g) {
        return jpa.save(GoalEntity.fromDomain(g)).toDomain();
    }

    @Override
    public Optional<Goal> findByIdAndUserUid(UUID id, String uid) {
        return jpa.findByIdAndUserUid(id, uid).map(GoalEntity::toDomain);
    }

    @Override
    public List<Goal> findAllByUserUid(String uid) {
        return jpa.findAllByUserUid(uid).stream()
                .map(GoalEntity::toDomain)
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
