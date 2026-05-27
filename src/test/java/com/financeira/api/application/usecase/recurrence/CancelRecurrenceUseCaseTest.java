package com.financeira.api.application.usecase.recurrence;

import com.financeira.api.application.dto.RecurrenceResponse;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Recurrence;
import com.financeira.api.domain.repository.RecurrenceRepository;
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
class CancelRecurrenceUseCaseTest {

    @Mock RecurrenceRepository repository;
    @InjectMocks CancelRecurrenceUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldSetActiveFalse_whenCancelling() {
        UUID id = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Recurrence recurrence = new Recurrence(USER_UID, "Netflix", "📺", catId,
                "debito_recorrente", BigDecimal.valueOf(55.90), null, null,
                "2026-01", null, 12, true);
        recurrence.setId(id);
        when(repository.findByIdAndUserUid(id, USER_UID)).thenReturn(Optional.of(recurrence));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RecurrenceResponse response = useCase.execute(USER_UID, id);

        assertThat(response.active()).isFalse();
        verify(repository).save(any());
    }

    @Test
    void shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndUserUid(id, USER_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(USER_UID, id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
