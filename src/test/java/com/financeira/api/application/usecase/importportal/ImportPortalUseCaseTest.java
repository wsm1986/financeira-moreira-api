package com.financeira.api.application.usecase.importportal;

import com.financeira.api.application.dto.ImportPortalRequest;
import com.financeira.api.application.dto.ImportPortalRequest.*;
import com.financeira.api.application.dto.ImportPortalResponse;
import com.financeira.api.domain.model.*;
import com.financeira.api.domain.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportPortalUseCaseTest {

    @Mock CategoryRepository categoryRepository;
    @Mock BankRepository bankRepository;
    @Mock CreditCardRepository cardRepository;
    @Mock EntryRepository entryRepository;
    @Mock RecurrenceRepository recurrenceRepository;
    @Mock BillRepository billRepository;
    @Mock InvestmentRepository investmentRepository;
    @Mock GoalRepository goalRepository;
    @Mock PayslipRepository payslipRepository;

    @InjectMocks ImportPortalUseCase useCase;

    private static final String USER_UID = "user-123";

    private void setupDefaultMocks() {
        when(categoryRepository.findAllByUserUid(USER_UID)).thenReturn(List.of());
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bankRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(entryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(payslipRepository.findByUserUidAndCompetencia(any(), any())).thenReturn(Optional.empty());
        when(payslipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void shouldImportOneCategory() {
        setupDefaultMocks();
        RawCategory rawCat = new RawCategory("cat-1", "Alimentação", "🍔", 1200.0, "#7c8dff", "expense", "essencial");
        ImportPortalRequest request = new ImportPortalRequest(null, List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(rawCat), List.of(), List.of(), List.of());

        ImportPortalResponse response = useCase.execute(USER_UID, request);

        assertThat(response.summary().get("categories").imported()).isEqualTo(1);
        assertThat(response.summary().get("categories").skipped()).isEqualTo(0);
        verify(categoryRepository, times(1)).save(any());
    }

    @Test
    void shouldSkipDuplicateCategory() {
        Category existing = new Category(USER_UID, "Alimentação", "🍔", BigDecimal.valueOf(1200), "#7c8dff", "expense", "essencial");
        when(categoryRepository.findAllByUserUid(USER_UID)).thenReturn(List.of(existing));

        RawCategory rawCat = new RawCategory("cat-1", "Alimentação", "🍔", 1200.0, "#7c8dff", "expense", "essencial");
        ImportPortalRequest request = new ImportPortalRequest(null, List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(rawCat), List.of(), List.of(), List.of());

        ImportPortalResponse response = useCase.execute(USER_UID, request);

        assertThat(response.summary().get("categories").imported()).isEqualTo(0);
        assertThat(response.summary().get("categories").skipped()).isEqualTo(1);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void shouldAddWarning_whenEntryHasUnknownCategory() {
        when(categoryRepository.findAllByUserUid(USER_UID)).thenReturn(List.of());
        RawEntry rawEntry = new RawEntry("e-1", "2026-05", "debito_avista", "Mercado",
                "Categoria Inexistente", 150.0, "2026-05-10", "🛒", null,
                null, null, null, null, null, null, null, null, null, false, false, null, null);
        ImportPortalRequest request = new ImportPortalRequest(null, List.of(rawEntry), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

        ImportPortalResponse response = useCase.execute(USER_UID, request);

        assertThat(response.summary().get("entries").skipped()).isEqualTo(1);
        assertThat(response.warnings()).hasSize(1);
        assertThat(response.warnings().get(0)).contains("Categoria Inexistente");
        verify(entryRepository, never()).save(any());
    }

    @Test
    void shouldImportOneBank_andOnePayslip() {
        setupDefaultMocks();
        RawBank rawBank = new RawBank("bank-1", "Nubank", "digital", 1000.0, "#820AD1", "🏦");
        RawPayslip rawPayslip = new RawPayslip("p-1", "2026-05", 8000.0, List.of(),
                600.0, 1200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, List.of(),
                640.0, 8500.0, 1800.0, 6700.0, null);
        ImportPortalRequest request = new ImportPortalRequest(null, List.of(), List.of(),
                List.of(rawBank), List.of(), List.of(), List.of(), List.of(), List.of(rawPayslip), List.of());

        ImportPortalResponse response = useCase.execute(USER_UID, request);

        assertThat(response.summary().get("banks").imported()).isEqualTo(1);
        assertThat(response.summary().get("payslips").imported()).isEqualTo(1);
        verify(bankRepository, times(1)).save(any());
        verify(payslipRepository, times(1)).save(any());
    }
}
