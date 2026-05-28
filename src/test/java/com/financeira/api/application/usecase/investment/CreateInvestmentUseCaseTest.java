package com.financeira.api.application.usecase.investment;

import com.financeira.api.application.dto.InvestmentRequest;
import com.financeira.api.application.dto.InvestmentResponse;
import com.financeira.api.domain.model.Investment;
import com.financeira.api.domain.repository.InvestmentRepository;
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
class CreateInvestmentUseCaseTest {

    @Mock InvestmentRepository repository;
    @InjectMocks CreateInvestmentUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldCreateInvestment() {
        InvestmentRequest req = new InvestmentRequest("Tesouro IPCA+", "renda_fixa",
                BigDecimal.valueOf(5000), BigDecimal.valueOf(5200), "6.5",
                null, null, false, "📈", "#34d399");
        Investment saved = new Investment(USER_UID, "Tesouro IPCA+", "renda_fixa",
                BigDecimal.valueOf(5000), BigDecimal.valueOf(5200), BigDecimal.valueOf(6.5),
                null, null, false, "📈", "#34d399");
        when(repository.save(any())).thenReturn(saved);

        InvestmentResponse response = useCase.execute(USER_UID, req);

        assertThat(response.name()).isEqualTo("Tesouro IPCA+");
        assertThat(response.type()).isEqualTo("renda_fixa");
        verify(repository, times(1)).save(any());
    }
}
