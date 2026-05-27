package com.financeira.api.infrastructure.persistence.repository;

import com.financeira.api.domain.model.Payslip;
import com.financeira.api.domain.repository.PayslipRepository;
import com.financeira.api.infrastructure.persistence.entity.PayslipEntity;
import com.financeira.api.infrastructure.persistence.jpa.PayslipJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class PayslipRepositoryImpl implements PayslipRepository {

    private final PayslipJpaRepository jpa;

    public PayslipRepositoryImpl(PayslipJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Payslip save(Payslip p) {
        return jpa.save(PayslipEntity.fromDomain(p)).toDomain();
    }

    @Override
    public Optional<Payslip> findByIdAndUserUid(UUID id, String uid) {
        return jpa.findByIdAndUserUid(id, uid).map(PayslipEntity::toDomain);
    }

    @Override
    public Optional<Payslip> findByUserUidAndCompetencia(String uid, String competencia) {
        return jpa.findByUserUidAndCompetencia(uid, competencia).map(PayslipEntity::toDomain);
    }

    @Override
    public List<Payslip> findAllByUserUid(String uid) {
        return jpa.findAllByUserUid(uid).stream()
                .map(PayslipEntity::toDomain)
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
