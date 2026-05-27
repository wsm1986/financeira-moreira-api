package com.financeira.api.application.usecase.bill;

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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListBillsUseCaseTest {

    @Mock BillRepository repository;
    @InjectMocks ListBillsUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldReturnAllBills() {
        UUID catId = UUID.randomUUID();
        List<Bill> bills = List.of(
                new Bill(USER_UID, "Aluguel", BigDecimal.valueOf(1500), LocalDate.now(), catId, false, null, null, null, "pagar"),
                new Bill(USER_UID, "Energia", BigDecimal.valueOf(200), LocalDate.now(), catId, false, null, null, null, "pagar")
        );
        when(repository.findAllByUserUid(USER_UID)).thenReturn(bills);

        List<BillResponse> result = useCase.execute(USER_UID);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BillResponse::name).containsExactlyInAnyOrder("Aluguel", "Energia");
    }
}
