package com.financeira.api.application.usecase.bank;

import com.financeira.api.application.dto.BankResponse;
import com.financeira.api.domain.model.Bank;
import com.financeira.api.domain.repository.BankRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListBanksUseCaseTest {

    @Mock BankRepository repository;
    @InjectMocks ListBanksUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldReturnAllBanks() {
        List<Bank> banks = List.of(
                new Bank(USER_UID, "Nubank", "digital", BigDecimal.valueOf(1000), "#820AD1", "🏦"),
                new Bank(USER_UID, "Itaú", "corrente", BigDecimal.valueOf(5000), "#EC7000", "🏦")
        );
        when(repository.findAllByUserUid(USER_UID)).thenReturn(banks);

        List<BankResponse> result = useCase.execute(USER_UID);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BankResponse::name).containsExactlyInAnyOrder("Nubank", "Itaú");
    }

    @Test
    void shouldReturnEmpty_whenNoBanks() {
        when(repository.findAllByUserUid(USER_UID)).thenReturn(List.of());

        List<BankResponse> result = useCase.execute(USER_UID);

        assertThat(result).isEmpty();
    }
}
