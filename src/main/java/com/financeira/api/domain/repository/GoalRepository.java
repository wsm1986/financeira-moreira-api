package com.financeira.api.domain.repository;

import com.financeira.api.domain.model.Goal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository {
    Goal save(Goal g);
    Optional<Goal> findByIdAndUserUid(UUID id, String uid);
    List<Goal> findAllByUserUid(String uid);
    void softDeleteByIdAndUserUid(UUID id, String uid);
    boolean existsByIdAndUserUid(UUID id, String uid);
}
