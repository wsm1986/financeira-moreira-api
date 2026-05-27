package com.financeira.api.application.usecase.bank;

import com.financeira.api.application.dto.BankRequest;
import com.financeira.api.application.dto.BankResponse;
import com.financeira.api.domain.model.Bank;
import com.financeira.api.domain.repository.BankRepository;
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
class CreateBankUseCaseTest {

    @Mock BankRepository repository;
    @InjectMocks CreateBankUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldCreateBank() {
        BankRequest req = new BankRequest("Nubank", "digital", BigDecimal.valueOf(1000), "#820AD1", "🏦");
        Bank saved = new Bank(USER_UID, "Nubank", "digital", BigDecimal.valueOf(1000), "#820AD1", "🏦");
        when(repository.save(any())).thenReturn(saved);

        BankResponse response = useCase.execute(USER_UID, req);

        assertThat(response.name()).isEqualTo("Nubank");
        assertThat(response.type()).isEqualTo("digital");
        verify(repository, times(1)).save(any());
    }

    @Test
    void shouldUseZeroBalance_whenBalanceIsNull() {
        BankRequest req = new BankRequest("Banco X", "corrente", null, "#fff", "🏦");
        Bank saved = new Bank(USER_UID, "Banco X", "corrente", BigDecimal.ZERO, "#fff", "🏦");
        when(repository.save(any())).thenReturn(saved);

        BankResponse response = useCase.execute(USER_UID, req);

        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
