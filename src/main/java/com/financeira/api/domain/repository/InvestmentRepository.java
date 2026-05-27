package com.financeira.api.domain.repository;

import com.financeira.api.domain.model.Investment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentRepository {
    Investment save(Investment inv);
    Optional<Investment> findByIdAndUserUid(UUID id, String uid);
    List<Investment> findAllByUserUid(String uid);
    void softDeleteByIdAndUserUid(UUID id, String uid);
    boolean existsByIdAndUserUid(UUID id, String uid);
}
