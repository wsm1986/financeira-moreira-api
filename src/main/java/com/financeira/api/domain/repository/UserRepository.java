package com.financeira.api.domain.repository;

import com.financeira.api.domain.model.User;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByUid(String uid);
    boolean existsByUid(String uid);
}
