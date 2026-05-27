package com.financeira.api.application.usecase.entry;

import com.financeira.api.application.dto.EntryRequest;
import com.financeira.api.application.dto.EntryResponse;
import com.financeira.api.domain.model.Entry;
import com.financeira.api.domain.repository.EntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateEntryUseCaseTest {

    @Mock EntryRepository repository;
    @InjectMocks CreateEntryUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldCreateEntry() {
        UUID categoryId = UUID.randomUUID();
        EntryRequest req = new EntryRequest("2026-05", "debito_avista", "Mercado",
                categoryId, BigDecimal.valueOf(150), LocalDate.of(2026, 5, 10),
                "🛒", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        Entry saved = new Entry(USER_UID, "2026-05", "debito_avista", "Mercado",
                categoryId, BigDecimal.valueOf(150), LocalDate.of(2026, 5, 10), "🛒");
        when(repository.save(any())).thenReturn(saved);

        EntryResponse response = useCase.execute(USER_UID, req);

        assertThat(response.name()).isEqualTo("Mercado");
        assertThat(response.monthKey()).isEqualTo("2026-05");
        verify(repository, times(1)).save(any());
    }

    @Test
    void shouldDefaultIsPaidToFalse_whenNotProvided() {
        UUID categoryId = UUID.randomUUID();
        EntryRequest req = new EntryRequest("2026-05", "debito_avista", "Mercado",
                categoryId, BigDecimal.valueOf(150), LocalDate.of(2026, 5, 10),
                "🛒", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        Entry saved = new Entry(USER_UID, "2026-05", "debito_avista", "Mercado",
                categoryId, BigDecimal.valueOf(150), LocalDate.of(2026, 5, 10), "🛒");
        when(repository.save(any())).thenReturn(saved);

        EntryResponse response = useCase.execute(USER_UID, req);

        assertThat(response.isPaid()).isFalse();
    }
}
