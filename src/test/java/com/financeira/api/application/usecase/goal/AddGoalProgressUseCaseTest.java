package com.financeira.api.application.usecase.goal;

import com.financeira.api.application.dto.GoalResponse;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Goal;
import com.financeira.api.domain.repository.GoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddGoalProgressUseCaseTest {

    @Mock GoalRepository repository;
    @InjectMocks AddGoalProgressUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldAddProgressToCurrentAmount() {
        UUID id = UUID.randomUUID();
        Goal goal = new Goal(USER_UID, "Viagem Europa", "✈️", BigDecimal.valueOf(15000),
                BigDecimal.valueOf(1000), "2027-06", "#7c8dff", "on-track", null);
        goal.setId(id);
        when(repository.findByIdAndUserUid(id, USER_UID)).thenReturn(Optional.of(goal));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GoalResponse response = useCase.execute(USER_UID, id, BigDecimal.valueOf(500));

        assertThat(response.currentAmount()).isEqualByComparingTo(BigDecimal.valueOf(1500));
        verify(repository).save(any());
    }

    @Test
    void shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndUserUid(id, USER_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(USER_UID, id, BigDecimal.valueOf(500)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
