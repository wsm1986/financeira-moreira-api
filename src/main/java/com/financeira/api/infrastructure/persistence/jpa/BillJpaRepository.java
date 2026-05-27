package com.financeira.api.infrastructure.persistence.jpa;

import com.financeira.api.infrastructure.persistence.entity.BillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillJpaRepository extends JpaRepository<BillEntity, UUID> {
    Optional<BillEntity> findByIdAndUserUid(UUID id, String userUid);
    List<BillEntity> findAllByUserUid(String userUid);
    boolean existsByIdAndUserUid(UUID id, String userUid);

    @Modifying
    @Query("UPDATE BillEntity e SET e.deletedAt = :now WHERE e.id = :id AND e.userUid = :uid")
    void softDeleteByIdAndUserUid(@Param("id") UUID id, @Param("uid") String uid, @Param("now") Instant now);
}
