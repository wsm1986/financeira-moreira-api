package com.financeira.api.application.usecase.category;

import com.financeira.api.application.dto.CategoryRequest;
import com.financeira.api.application.dto.CategoryResponse;
import com.financeira.api.domain.exception.BusinessException;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Category;
import com.financeira.api.domain.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCategoryUseCaseTest {

    @Mock CategoryRepository repository;
    @InjectMocks UpdateCategoryUseCase useCase;

    private static final String USER_UID = "user-123";

    private Category buildCategory(UUID id, String name) {
        Category c = new Category(USER_UID, name, "🏠", BigDecimal.valueOf(2500), "#f87171", "expense", "essencial");
        c.setId(id);
        return c;
    }

    @Test
    void shouldUpdateCategory_whenExists() {
        UUID id = UUID.randomUUID();
        Category existing = buildCategory(id, "Moradia");
        Category updated  = buildCategory(id, "Casa");
        CategoryRequest req = new CategoryRequest("Casa", "🏡", BigDecimal.valueOf(3000), "#fff", "expense", "essencial");

        when(repository.findByIdAndUserUid(id, USER_UID)).thenReturn(Optional.of(existing));
        when(repository.findByNameAndUserUid("Casa", USER_UID)).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(updated);

        CategoryResponse response = useCase.execute(USER_UID, id, req);

        assertThat(response.name()).isEqualTo("Casa");
        verify(repository, times(1)).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndUserUid(id, USER_UID)).thenReturn(Optional.empty());

        CategoryRequest req = new CategoryRequest("X", "🏠", BigDecimal.ZERO, "#fff", "expense", null);
        assertThatThrownBy(() -> useCase.execute(USER_UID, id, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowBusinessException_whenNameConflictsWithAnotherCategory() {
        UUID id  = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Category existing = buildCategory(id, "Moradia");
        Category other    = buildCategory(id2, "Saúde");

        when(repository.findByIdAndUserUid(id, USER_UID)).thenReturn(Optional.of(existing));
        when(repository.findByNameAndUserUid("Saúde", USER_UID)).thenReturn(Optional.of(other));

        CategoryRequest req = new CategoryRequest("Saúde", "💊", BigDecimal.ZERO, "#fff", "expense", "essencial");
        assertThatThrownBy(() -> useCase.execute(USER_UID, id, req))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldAllowSameNameOnSameCategory() {
        UUID id = UUID.randomUUID();
        Category existing = buildCategory(id, "Moradia");
        Category updated  = buildCategory(id, "Moradia");
        CategoryRequest req = new CategoryRequest("Moradia", "🏠", BigDecimal.valueOf(2800), "#eee", "expense", "essencial");

        when(repository.findByIdAndUserUid(id, USER_UID)).thenReturn(Optional.of(existing));
        when(repository.findByNameAndUserUid("Moradia", USER_UID)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(updated);

        assertThatNoException().isThrownBy(() -> useCase.execute(USER_UID, id, req));
    }
}
