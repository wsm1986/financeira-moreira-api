package com.financeira.api.domain.repository;

import com.financeira.api.domain.model.CreditCard;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardRepository {
    CreditCard save(CreditCard c);
    Optional<CreditCard> findByIdAndUserUid(UUID id, String uid);
    List<CreditCard> findAllByUserUid(String uid);
    void softDeleteByIdAndUserUid(UUID id, String uid);
    boolean existsByIdAndUserUid(UUID id, String uid);
}
