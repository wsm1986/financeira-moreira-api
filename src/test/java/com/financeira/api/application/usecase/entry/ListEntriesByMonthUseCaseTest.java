package com.financeira.api.application.usecase.entry;

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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListEntriesByMonthUseCaseTest {

    @Mock EntryRepository repository;
    @InjectMocks ListEntriesByMonthUseCase useCase;

    private static final String USER_UID = "user-123";
    private static final String MONTH_KEY = "2026-05";

    @Test
    void shouldReturnEntriesForMonth() {
        UUID catId = UUID.randomUUID();
        List<Entry> entries = List.of(
                new Entry(USER_UID, MONTH_KEY, "debito_avista", "Mercado", catId, BigDecimal.valueOf(150), LocalDate.now(), "🛒"),
                new Entry(USER_UID, MONTH_KEY, "receita", "Salário", catId, BigDecimal.valueOf(5000), LocalDate.now(), "💰")
        );
        when(repository.findAllByUserUidAndMonthKey(USER_UID, MONTH_KEY)).thenReturn(entries);

        List<EntryResponse> result = useCase.execute(USER_UID, MONTH_KEY);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(EntryResponse::name).containsExactlyInAnyOrder("Mercado", "Salário");
    }

    @Test
    void shouldReturnEmpty_whenNoEntries() {
        when(repository.findAllByUserUidAndMonthKey(USER_UID, MONTH_KEY)).thenReturn(List.of());

        List<EntryResponse> result = useCase.execute(USER_UID, MONTH_KEY);

        assertThat(result).isEmpty();
    }
}
