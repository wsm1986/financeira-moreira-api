package com.financeira.api.application.usecase.investment;

import com.financeira.api.application.dto.InvestmentResponse;
import com.financeira.api.domain.model.Investment;
import com.financeira.api.domain.repository.InvestmentRepository;
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
class ListInvestmentsUseCaseTest {

    @Mock InvestmentRepository repository;
    @InjectMocks ListInvestmentsUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldReturnAllInvestments() {
        List<Investment> investments = List.of(
                new Investment(USER_UID, "Tesouro IPCA+", "renda_fixa", BigDecimal.valueOf(5000),
                        BigDecimal.valueOf(5200), null, null, null, false, "📈", "#34d399"),
                new Investment(USER_UID, "FII KNRI11", "fundo", BigDecimal.valueOf(3000),
                        BigDecimal.valueOf(3100), null, null, null, false, "🏢", "#7c8dff")
        );
        when(repository.findAllByUserUid(USER_UID)).thenReturn(investments);

        List<InvestmentResponse> result = useCase.execute(USER_UID);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(InvestmentResponse::name).containsExactlyInAnyOrder("Tesouro IPCA+", "FII KNRI11");
    }
}
