package com.financeira.api.domain.repository;

import com.financeira.api.domain.model.Bank;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankRepository {
    Bank save(Bank b);
    Optional<Bank> findByIdAndUserUid(UUID id, String uid);
    List<Bank> findAllByUserUid(String uid);
    void softDeleteByIdAndUserUid(UUID id, String uid);
    boolean existsByIdAndUserUid(UUID id, String uid);
}
