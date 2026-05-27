package com.financeira.api.application.usecase.recurrence;

import com.financeira.api.application.dto.RecurrenceRequest;
import com.financeira.api.application.dto.RecurrenceResponse;
import com.financeira.api.domain.model.Recurrence;
import com.financeira.api.domain.repository.RecurrenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateRecurrenceUseCaseTest {

    @Mock RecurrenceRepository repository;
    @InjectMocks CreateRecurrenceUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldCreateRecurrence() {
        UUID categoryId = UUID.randomUUID();
        RecurrenceRequest req = new RecurrenceRequest("Netflix", "📺", categoryId,
                "debito_recorrente", BigDecimal.valueOf(55.90), null, null,
                "2026-01", null, 12, true);
        Recurrence saved = new Recurrence(USER_UID, "Netflix", "📺", categoryId,
                "debito_recorrente", BigDecimal.valueOf(55.90), null, null,
                "2026-01", null, 12, true);
        when(repository.save(any())).thenReturn(saved);

        RecurrenceResponse response = useCase.execute(USER_UID, req);

        assertThat(response.name()).isEqualTo("Netflix");
        assertThat(response.active()).isTrue();
        verify(repository, times(1)).save(any());
    }
}
