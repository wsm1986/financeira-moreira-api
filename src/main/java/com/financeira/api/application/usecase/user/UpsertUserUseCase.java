package com.financeira.api.application.usecase.user;

import com.financeira.api.domain.model.User;
import com.financeira.api.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UpsertUserUseCase {

    private final UserRepository repository;

    public UpsertUserUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public User execute(String uid, String email, String name) {
        return repository.findByUid(uid).orElseGet(() -> {
            User user = new User(uid, email, name);
            return repository.save(user);
        });
    }
}
