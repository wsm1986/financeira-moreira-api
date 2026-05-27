package com.financeira.api.application.usecase.payslip;

import com.financeira.api.application.dto.PayslipItemDto;
import com.financeira.api.application.dto.PayslipRequest;
import com.financeira.api.application.dto.PayslipResponse;
import com.financeira.api.domain.model.Payslip;
import com.financeira.api.domain.repository.PayslipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavePayslipUseCaseTest {

    @Mock PayslipRepository repository;
    @InjectMocks SavePayslipUseCase useCase;

    private static final String USER_UID = "user-123";

    private PayslipRequest buildRequest(String competencia) {
        return new PayslipRequest(competencia, BigDecimal.valueOf(8000),
                List.of(new PayslipItemDto("Bônus", BigDecimal.valueOf(500))),
                BigDecimal.valueOf(600), BigDecimal.valueOf(1200), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, List.of(), BigDecimal.valueOf(640),
                BigDecimal.valueOf(8500), BigDecimal.valueOf(1800), BigDecimal.valueOf(6700), null);
    }

    @Test
    void shouldCreateNewPayslip_whenCompetenciaDoesNotExist() {
        PayslipRequest req = buildRequest("2026-05");
        when(repository.findByUserUidAndCompetencia(USER_UID, "2026-05")).thenReturn(Optional.empty());
        Payslip saved = new Payslip(USER_UID, "2026-05", BigDecimal.valueOf(8000),
                BigDecimal.valueOf(8500), BigDecimal.valueOf(1800), BigDecimal.valueOf(6700));
        when(repository.save(any())).thenReturn(saved);

        PayslipResponse response = useCase.execute(USER_UID, req);

        assertThat(response.competencia()).isEqualTo("2026-05");
        verify(repository, times(1)).save(any());
    }

    @Test
    void shouldUpdateExistingPayslip_whenCompetenciaAlreadyExists() {
        PayslipRequest req = buildRequest("2026-05");
        Payslip existing = new Payslip(USER_UID, "2026-05", BigDecimal.valueOf(7500),
                BigDecimal.valueOf(8000), BigDecimal.valueOf(1600), BigDecimal.valueOf(6400));
        when(repository.findByUserUidAndCompetencia(USER_UID, "2026-05")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PayslipResponse response = useCase.execute(USER_UID, req);

        assertThat(response.salarioBase()).isEqualByComparingTo(BigDecimal.valueOf(8000));
        verify(repository, times(1)).save(any());
    }
}
