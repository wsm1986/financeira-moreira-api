package com.financeira.api.domain.repository;

import com.financeira.api.domain.model.Payslip;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayslipRepository {
    Payslip save(Payslip p);
    Optional<Payslip> findByIdAndUserUid(UUID id, String uid);
    Optional<Payslip> findByUserUidAndCompetencia(String uid, String competencia);
    List<Payslip> findAllByUserUid(String uid);
    void softDeleteByIdAndUserUid(UUID id, String uid);
    boolean existsByIdAndUserUid(UUID id, String uid);
}
