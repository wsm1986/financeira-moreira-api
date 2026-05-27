package com.financeira.api.application.usecase.card;

import com.financeira.api.application.dto.CardResponse;
import com.financeira.api.domain.model.CreditCard;
import com.financeira.api.domain.repository.CreditCardRepository;
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
class ListCardsUseCaseTest {

    @Mock CreditCardRepository repository;
    @InjectMocks ListCardsUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldReturnAllCards() {
        List<CreditCard> cards = List.of(
                new CreditCard(USER_UID, "Nubank", "mastercard", "1234", BigDecimal.valueOf(5000), 10, 20, "#820AD1", "💳", null),
                new CreditCard(USER_UID, "Itaú", "visa", "5678", BigDecimal.valueOf(10000), 5, 15, "#EC7000", "💳", null)
        );
        when(repository.findAllByUserUid(USER_UID)).thenReturn(cards);

        List<CardResponse> result = useCase.execute(USER_UID);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CardResponse::name).containsExactlyInAnyOrder("Nubank", "Itaú");
    }
}
