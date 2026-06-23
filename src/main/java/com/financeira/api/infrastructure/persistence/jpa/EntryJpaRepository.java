package com.financeira.api.infrastructure.persistence.jpa;

import com.financeira.api.infrastructure.persistence.entity.EntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntryJpaRepository extends JpaRepository<EntryEntity, UUID> {
    Optional<EntryEntity> findByIdAndUserUid(UUID id, String userUid);
    List<EntryEntity> findAllByUserUidAndMonthKey(String userUid, String monthKey);
    // Busca todos os lançamentos do ano com uma única query: WHERE month_key LIKE '2026%'
    List<EntryEntity> findAllByUserUidAndMonthKeyStartingWith(String userUid, String yearPrefix);
    boolean existsByIdAndUserUid(UUID id, String userUid);

    @Modifying
    @Query("UPDATE EntryEntity e SET e.deletedAt = :now WHERE e.id = :id AND e.userUid = :uid")
    void softDeleteByIdAndUserUid(@Param("id") UUID id, @Param("uid") String uid, @Param("now") Instant now);
}
