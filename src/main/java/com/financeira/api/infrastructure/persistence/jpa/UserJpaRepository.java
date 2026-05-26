package com.financeira.api.infrastructure.persistence.jpa;

import com.financeira.api.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUid(String uid);
    boolean existsByUid(String uid);
}
