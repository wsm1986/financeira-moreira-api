package com.financeira.api.infrastructure.persistence.jpa;

import com.financeira.api.infrastructure.persistence.entity.InvestmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentJpaRepository extends JpaRepository<InvestmentEntity, UUID> {
    Optional<InvestmentEntity> findByIdAndUserUid(UUID id, String userUid);
    List<InvestmentEntity> findAllByUserUid(String userUid);
    boolean existsByIdAndUserUid(UUID id, String userUid);

    @Modifying
    @Query("UPDATE InvestmentEntity e SET e.deletedAt = :now WHERE e.id = :id AND e.userUid = :uid")
    void softDeleteByIdAndUserUid(@Param("id") UUID id, @Param("uid") String uid, @Param("now") Instant now);
}
