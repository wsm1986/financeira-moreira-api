package com.financeira.api.infrastructure.persistence.jpa;

import com.financeira.api.infrastructure.persistence.entity.BankEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankJpaRepository extends JpaRepository<BankEntity, UUID> {
    Optional<BankEntity> findByIdAndUserUid(UUID id, String userUid);
    List<BankEntity> findAllByUserUid(String userUid);
    boolean existsByIdAndUserUid(UUID id, String userUid);

    @Modifying
    @Query("UPDATE BankEntity e SET e.deletedAt = :now WHERE e.id = :id AND e.userUid = :uid")
    void softDeleteByIdAndUserUid(@Param("id") UUID id, @Param("uid") String uid, @Param("now") Instant now);
}
