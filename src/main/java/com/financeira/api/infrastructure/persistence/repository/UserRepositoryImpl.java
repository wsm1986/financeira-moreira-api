package com.financeira.api.infrastructure.persistence.repository;

import com.financeira.api.domain.model.User;
import com.financeira.api.domain.repository.UserRepository;
import com.financeira.api.infrastructure.persistence.entity.UserEntity;
import com.financeira.api.infrastructure.persistence.jpa.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpa;

    public UserRepositoryImpl(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public User save(User user) {
        return jpa.save(UserEntity.fromDomain(user)).toDomain();
    }

    @Override
    public Optional<User> findByUid(String uid) {
        return jpa.findByUid(uid).map(UserEntity::toDomain);
    }

    @Override
    public boolean existsByUid(String uid) {
        return jpa.existsByUid(uid);
    }
}
