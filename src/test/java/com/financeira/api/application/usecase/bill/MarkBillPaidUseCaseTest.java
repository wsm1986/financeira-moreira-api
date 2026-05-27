package com.financeira.api.application.usecase.bill;

import com.financeira.api.application.dto.BillResponse;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Bill;
import com.financeira.api.domain.repository.BillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarkBillPaidUseCaseTest {

    @Mock BillRepository repository;
    @InjectMocks MarkBillPaidUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldMarkAsPaid_andSetPaidDate() {
        UUID id = UUID.randomUUID();
        UUID catId = UUID.randomUUID();
        Bill bill = new Bill(USER_UID, "Aluguel", BigDecimal.valueOf(1500), LocalDate.of(2026, 6, 10),
                catId, false, null, null, null, "pagar");
        bill.setId(id);
        when(repository.findByIdAndUserUid(id, USER_UID)).thenReturn(Optional.of(bill));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BillResponse response = useCase.execute(USER_UID, id);

        assertThat(response.paid()).isTrue();
        assertThat(response.paidDate()).isNotNull();
        verify(repository).save(any());
    }

    @Test
    void shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndUserUid(id, USER_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(USER_UID, id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
