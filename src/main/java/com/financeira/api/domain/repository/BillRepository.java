package com.financeira.api.domain.repository;

import com.financeira.api.domain.model.Bill;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillRepository {
    Bill save(Bill b);
    Optional<Bill> findByIdAndUserUid(UUID id, String uid);
    List<Bill> findAllByUserUid(String uid);
    void softDeleteByIdAndUserUid(UUID id, String uid);
    boolean existsByIdAndUserUid(UUID id, String uid);
}
