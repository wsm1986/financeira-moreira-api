package com.financeira.api.infrastructure.persistence.repository;

import com.financeira.api.domain.model.Investment;
import com.financeira.api.domain.repository.InvestmentRepository;
import com.financeira.api.infrastructure.persistence.entity.InvestmentEntity;
import com.financeira.api.infrastructure.persistence.jpa.InvestmentJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class InvestmentRepositoryImpl implements InvestmentRepository {

    private final InvestmentJpaRepository jpa;

    public InvestmentRepositoryImpl(InvestmentJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Investment save(Investment inv) {
        return jpa.save(InvestmentEntity.fromDomain(inv)).toDomain();
    }

    @Override
    public Optional<Investment> findByIdAndUserUid(UUID id, String uid) {
        return jpa.findByIdAndUserUid(id, uid).map(InvestmentEntity::toDomain);
    }

    @Override
    public List<Investment> findAllByUserUid(String uid) {
        return jpa.findAllByUserUid(uid).stream()
                .map(InvestmentEntity::toDomain)
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
