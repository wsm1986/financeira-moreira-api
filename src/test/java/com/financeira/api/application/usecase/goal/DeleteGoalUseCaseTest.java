package com.financeira.api.application.usecase.goal;

import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.repository.GoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteGoalUseCaseTest {

    @Mock GoalRepository repository;
    @InjectMocks DeleteGoalUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldSoftDelete_whenExists() {
        UUID id = UUID.randomUUID();
        when(repository.existsByIdAndUserUid(id, USER_UID)).thenReturn(true);

        assertThatNoException().isThrownBy(() -> useCase.execute(USER_UID, id));

        verify(repository).softDeleteByIdAndUserUid(id, USER_UID);
    }

    @Test
    void shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.existsByIdAndUserUid(id, USER_UID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(USER_UID, id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
