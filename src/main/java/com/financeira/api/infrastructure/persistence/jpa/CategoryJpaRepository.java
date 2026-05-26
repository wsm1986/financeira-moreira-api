package com.financeira.api.infrastructure.persistence.jpa;

import com.financeira.api.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {
    Optional<CategoryEntity> findByIdAndUserUid(UUID id, String userUid);
    Optional<CategoryEntity> findByNameAndUserUid(String name, String userUid);
    List<CategoryEntity> findAllByUserUid(String userUid);
    boolean existsByIdAndUserUid(UUID id, String userUid);
    void deleteByIdAndUserUid(UUID id, String userUid);
}
