package com.financeira.api.infrastructure.persistence.repository;

import com.financeira.api.domain.model.Bill;
import com.financeira.api.domain.repository.BillRepository;
import com.financeira.api.infrastructure.persistence.entity.BillEntity;
import com.financeira.api.infrastructure.persistence.jpa.BillJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class BillRepositoryImpl implements BillRepository {

    private final BillJpaRepository jpa;

    public BillRepositoryImpl(BillJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Bill save(Bill b) {
        return jpa.save(BillEntity.fromDomain(b)).toDomain();
    }

    @Override
    public Optional<Bill> findByIdAndUserUid(UUID id, String uid) {
        return jpa.findByIdAndUserUid(id, uid).map(BillEntity::toDomain);
    }

    @Override
    public List<Bill> findAllByUserUid(String uid) {
        return jpa.findAllByUserUid(uid).stream()
                .map(BillEntity::toDomain)
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
