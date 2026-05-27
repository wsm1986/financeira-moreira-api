package com.financeira.api.application.usecase.recurrence;

import com.financeira.api.application.dto.RecurrenceResponse;
import com.financeira.api.domain.model.Recurrence;
import com.financeira.api.domain.repository.RecurrenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListRecurrencesUseCaseTest {

    @Mock RecurrenceRepository repository;
    @InjectMocks ListRecurrencesUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldReturnAllRecurrences() {
        UUID catId = UUID.randomUUID();
        List<Recurrence> recs = List.of(
                new Recurrence(USER_UID, "Netflix", "📺", catId, "debito_recorrente",
                        BigDecimal.valueOf(55.90), null, null, "2026-01", null, 12, true),
                new Recurrence(USER_UID, "Spotify", "🎵", catId, "debito_recorrente",
                        BigDecimal.valueOf(21.90), null, null, "2026-01", null, 12, true)
        );
        when(repository.findAllByUserUid(USER_UID)).thenReturn(recs);

        List<RecurrenceResponse> result = useCase.execute(USER_UID);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RecurrenceResponse::name).containsExactlyInAnyOrder("Netflix", "Spotify");
    }
}
