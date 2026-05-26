package com.financeira.api.application.usecase.category;

import com.financeira.api.application.dto.CategoryResponse;
import com.financeira.api.domain.model.Category;
import com.financeira.api.domain.repository.CategoryRepository;
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
class ListCategoriesUseCaseTest {

    @Mock CategoryRepository repository;
    @InjectMocks ListCategoriesUseCase useCase;

    private static final String USER_UID = "user-123";

    @Test
    void shouldReturnAllCategories() {
        List<Category> cats = List.of(
                new Category(USER_UID, "Moradia",    "🏠", BigDecimal.valueOf(2500), "#f87171", "expense", "essencial"),
                new Category(USER_UID, "Alimentação","🍔", BigDecimal.valueOf(1200), "#7c8dff", "expense", "essencial")
        );
        when(repository.findAllByUserUid(USER_UID)).thenReturn(cats);

        List<CategoryResponse> result = useCase.execute(USER_UID);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CategoryResponse::name)
                .containsExactly("Moradia", "Alimentação");
    }

    @Test
    void shouldReturnEmptyList_whenNoCategories() {
        when(repository.findAllByUserUid(USER_UID)).thenReturn(List.of());

        List<CategoryResponse> result = useCase.execute(USER_UID);

        assertThat(result).isEmpty();
    }
}
