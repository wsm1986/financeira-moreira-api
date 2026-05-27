package com.financeira.api.infrastructure.persistence.repository;

import com.financeira.api.domain.model.CreditCard;
import com.financeira.api.domain.repository.CreditCardRepository;
import com.financeira.api.infrastructure.persistence.entity.CreditCardEntity;
import com.financeira.api.infrastructure.persistence.jpa.CreditCardJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CreditCardRepositoryImpl implements CreditCardRepository {

    private final CreditCardJpaRepository jpa;

    public CreditCardRepositoryImpl(CreditCardJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public CreditCard save(CreditCard c) {
        return jpa.save(CreditCardEntity.fromDomain(c)).toDomain();
    }

    @Override
    public Optional<CreditCard> findByIdAndUserUid(UUID id, String uid) {
        return jpa.findByIdAndUserUid(id, uid).map(CreditCardEntity::toDomain);
    }

    @Override
    public List<CreditCard> findAllByUserUid(String uid) {
        return jpa.findAllByUserUid(uid).stream()
                .map(CreditCardEntity::toDomain)
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
