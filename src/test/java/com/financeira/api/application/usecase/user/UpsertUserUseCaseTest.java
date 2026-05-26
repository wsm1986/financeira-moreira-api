package com.financeira.api.application.usecase.user;

import com.financeira.api.domain.model.User;
import com.financeira.api.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpsertUserUseCaseTest {

    @Mock UserRepository repository;
    @InjectMocks UpsertUserUseCase useCase;

    @Test
    void shouldReturnExistingUser_whenAlreadyExists() {
        User existing = new User("uid-1", "user@test.com", "Test");
        when(repository.findByUid("uid-1")).thenReturn(Optional.of(existing));

        User result = useCase.execute("uid-1", "user@test.com", "Test");

        assertThat(result.getUid()).isEqualTo("uid-1");
        verify(repository, never()).save(any());
    }

    @Test
    void shouldCreateUser_whenNotFound() {
        User newUser = new User("uid-2", "new@test.com", "New");
        when(repository.findByUid("uid-2")).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(newUser);

        User result = useCase.execute("uid-2", "new@test.com", "New");

        assertThat(result.getEmail()).isEqualTo("new@test.com");
        verify(repository, times(1)).save(any());
    }
}
