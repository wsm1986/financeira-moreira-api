package com.financeira.api.application.usecase.category;

import com.financeira.api.application.dto.CategoryRequest;
import com.financeira.api.application.dto.CategoryResponse;
import com.financeira.api.domain.exception.BusinessException;
import com.financeira.api.domain.model.Category;
import com.financeira.api.domain.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCategoryUseCaseTest {

    @Mock CategoryRepository repository;
    @InjectMocks CreateCategoryUseCase useCase;

    private static final String USER_UID = "user-123";

    private CategoryRequest buildRequest(String name) {
        return new CategoryRequest(name, "🏠", BigDecimal.valueOf(2500), "#f87171", "expense", "essencial");
    }

    private Category buildCategory(String name) {
        return new Category(USER_UID, name, "🏠", BigDecimal.valueOf(2500), "#f87171", "expense", "essencial");
    }

    @Test
    void shouldCreateCategory_whenNameIsUnique() {
        CategoryRequest req = buildRequest("Moradia");
        Category saved = buildCategory("Moradia");

        when(repository.findByNameAndUserUid("Moradia", USER_UID)).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(saved);

        CategoryResponse response = useCase.execute(USER_UID, req);

        assertThat(response.name()).isEqualTo("Moradia");
        verify(repository, times(1)).save(any());
    }

    @Test
    void shouldThrowBusinessException_whenNameAlreadyExists() {
        CategoryRequest req = buildRequest("Moradia");
        when(repository.findByNameAndUserUid("Moradia", USER_UID))
                .thenReturn(Optional.of(buildCategory("Moradia")));

        assertThatThrownBy(() -> useCase.execute(USER_UID, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Moradia");

        verify(repository, never()).save(any());
    }

    @Test
    void shouldUseBudgetZero_whenBudgetIsNull() {
        CategoryRequest req = new CategoryRequest("Nova", "💳", null, "#fff", "both", null);
        Category saved = new Category(USER_UID, "Nova", "💳", BigDecimal.ZERO, "#fff", "both", null);

        when(repository.findByNameAndUserUid(anyString(), anyString())).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(saved);

        CategoryResponse response = useCase.execute(USER_UID, req);

        assertThat(response.budget()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
