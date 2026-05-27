package com.financeira.api.infrastructure.persistence.repository;

import com.financeira.api.domain.model.Bank;
import com.financeira.api.domain.repository.BankRepository;
import com.financeira.api.infrastructure.persistence.entity.BankEntity;
import com.financeira.api.infrastructure.persistence.jpa.BankJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class BankRepositoryImpl implements BankRepository {

    private final BankJpaRepository jpa;

    public BankRepositoryImpl(BankJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Bank save(Bank b) {
        return jpa.save(BankEntity.fromDomain(b)).toDomain();
    }

    @Override
    public Optional<Bank> findByIdAndUserUid(UUID id, String uid) {
        return jpa.findByIdAndUserUid(id, uid).map(BankEntity::toDomain);
    }

    @Override
    public List<Bank> findAllByUserUid(String uid) {
        return jpa.findAllByUserUid(uid).stream()
                .map(BankEntity::toDomain)
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
