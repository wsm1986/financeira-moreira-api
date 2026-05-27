package com.financeira.api.application.usecase.card;

import com.financeira.api.application.dto.CardRequest;
import com.financeira.api.application.dto.CardResponse;
import com.financeira.api.domain.model.CreditCard;
import com.financeira.api.domain.repository.CreditCardRepository;
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
class CreateCardUseCaseTest {

    @Mock CreditCardRepository repository;
    @InjectMocks CreateCardUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldCreateCard() {
        CardRequest req = new CardRequest("Nubank", "mastercard", "1234",
                BigDecimal.valueOf(5000), 10, 20, "#820AD1", "💳", null);
        CreditCard saved = new CreditCard(USER_UID, "Nubank", "mastercard", "1234",
                BigDecimal.valueOf(5000), 10, 20, "#820AD1", "💳", null);
        when(repository.save(any())).thenReturn(saved);

        CardResponse response = useCase.execute(USER_UID, req);

        assertThat(response.name()).isEqualTo("Nubank");
        assertThat(response.brand()).isEqualTo("mastercard");
        verify(repository, times(1)).save(any());
    }
}
