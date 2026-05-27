package com.financeira.api.application.usecase.bill;

import com.financeira.api.application.dto.BillRequest;
import com.financeira.api.application.dto.BillResponse;
import com.financeira.api.domain.model.Bill;
import com.financeira.api.domain.repository.BillRepository;
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
class CreateBillUseCaseTest {

    @Mock BillRepository repository;
    @InjectMocks CreateBillUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldCreateBill() {
        UUID catId = UUID.randomUUID();
        BillRequest req = new BillRequest("Aluguel", BigDecimal.valueOf(1500), LocalDate.of(2026, 6, 10),
                catId, null, null, null, null, "pagar");
        Bill saved = new Bill(USER_UID, "Aluguel", BigDecimal.valueOf(1500), LocalDate.of(2026, 6, 10),
                catId, false, null, null, null, "pagar");
        when(repository.save(any())).thenReturn(saved);

        BillResponse response = useCase.execute(USER_UID, req);

        assertThat(response.name()).isEqualTo("Aluguel");
        assertThat(response.paid()).isFalse();
        verify(repository, times(1)).save(any());
    }

    @Test
    void shouldDefaultTypeToPagar_whenTypeIsNull() {
        UUID catId = UUID.randomUUID();
        BillRequest req = new BillRequest("Telefone", BigDecimal.valueOf(100), LocalDate.now(),
                catId, null, null, null, null, null);
        Bill saved = new Bill(USER_UID, "Telefone", BigDecimal.valueOf(100), LocalDate.now(),
                catId, false, null, null, null, "pagar");
        when(repository.save(any())).thenReturn(saved);

        BillResponse response = useCase.execute(USER_UID, req);

        assertThat(response.type()).isEqualTo("pagar");
    }
}
