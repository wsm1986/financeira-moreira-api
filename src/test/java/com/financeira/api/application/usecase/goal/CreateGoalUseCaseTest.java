package com.financeira.api.application.usecase.goal;

import com.financeira.api.application.dto.GoalRequest;
import com.financeira.api.application.dto.GoalResponse;
import com.financeira.api.domain.model.Goal;
import com.financeira.api.domain.repository.GoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateGoalUseCaseTest {

    @Mock GoalRepository repository;
    @InjectMocks CreateGoalUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldCreateGoal() {
        GoalRequest req = new GoalRequest("Viagem Europa", "✈️", BigDecimal.valueOf(15000),
                BigDecimal.ZERO, "2027-06", "#7c8dff", "on-track", null);
        Goal saved = new Goal(USER_UID, "Viagem Europa", "✈️", BigDecimal.valueOf(15000),
                BigDecimal.ZERO, "2027-06", "#7c8dff", "on-track", null);
        when(repository.save(any())).thenReturn(saved);

        GoalResponse response = useCase.execute(USER_UID, req);

        assertThat(response.name()).isEqualTo("Viagem Europa");
        assertThat(response.status()).isEqualTo("on-track");
        verify(repository, times(1)).save(any());
    }
}
