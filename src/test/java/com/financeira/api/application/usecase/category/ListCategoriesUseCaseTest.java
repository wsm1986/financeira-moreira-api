package com.financeira.api.application.usecase.category;

import com.financeira.api.application.dto.CategoryResponse;
import com.financeira.api.domain.model.Category;
import com.financeira.api.domain.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListCategoriesUseCaseTest {

    @Mock CategoryRepository repository;
    @InjectMocks ListCategoriesUseCase useCase;

    private static final String USER_UID = "user-123";

    // ── Cenários: usuário com categorias existentes ───────────────────

    @Test
    void shouldReturnAllCategories_whenUserHasExistingCategories() {
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
    void shouldNotCallSave_whenAllNamesAreAlreadyCorrect() {
        List<Category> cats = List.of(
            new Category(USER_UID, "Moradia",    "🏠", BigDecimal.valueOf(2500), "#f87171", "expense", "essencial"),
            new Category(USER_UID, "Assinaturas","📺", BigDecimal.valueOf(300),  "#9499b0", "expense", "desejo")
        );
        when(repository.findAllByUserUid(USER_UID)).thenReturn(cats);

        useCase.execute(USER_UID);

        // fixAccents não deve chamar save quando nenhum nome precisa de correção
        verify(repository, never()).save(any());
    }

    // ── Cenários: fixAccents — migração de acentos ───────────────────

    @Test
    void shouldFixAccents_whenCategoryNameHasNoAccent() {
        Category broken = new Category(USER_UID, "Alimentacao", "🍔", BigDecimal.valueOf(1200), "#7c8dff", "expense", "essencial");
        when(repository.findAllByUserUid(USER_UID)).thenReturn(List.of(broken));

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        when(repository.save(captor.capture())).thenReturn(broken);

        List<CategoryResponse> result = useCase.execute(USER_UID);

        // Nome deve ter sido corrigido antes de salvar
        assertThat(captor.getValue().getName()).isEqualTo("Alimentação");
        // Resposta deve conter o nome corrigido
        assertThat(result).extracting(CategoryResponse::name).containsExactly("Alimentação");
    }

    @Test
    void shouldFixAllBrokenAccents_inSinglePass() {
        List<Category> broken = List.of(
            new Category(USER_UID, "Alimentacao",   "🍔", BigDecimal.valueOf(1200), "#7c8dff", "expense", "essencial"),
            new Category(USER_UID, "Saude",         "💊", BigDecimal.valueOf(400),  "#34d399", "expense", "essencial"),
            new Category(USER_UID, "Salario/Renda", "💰", BigDecimal.ZERO,          "#22c55e", "income",  null),
            new Category(USER_UID, "Transferencia", "📦", BigDecimal.ZERO,          "#fff",    "both",    null)
        );
        when(repository.findAllByUserUid(USER_UID)).thenReturn(broken);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<CategoryResponse> result = useCase.execute(USER_UID);

        // Todos os 4 devem ter sido salvos com nomes corrigidos
        verify(repository, times(4)).save(any());
        assertThat(result).extracting(CategoryResponse::name)
                .containsExactlyInAnyOrder("Alimentação", "Saúde", "Salário/Renda", "Transferência");
    }

    @Test
    void shouldOnlySaveBrokenOnes_whenMixedList() {
        List<Category> mixed = List.of(
            new Category(USER_UID, "Moradia",    "🏠", BigDecimal.valueOf(2500), "#f87171", "expense", "essencial"),  // OK
            new Category(USER_UID, "Saude",      "💊", BigDecimal.valueOf(400),  "#34d399", "expense", "essencial"),  // broken
            new Category(USER_UID, "Assinaturas","📺", BigDecimal.valueOf(300),  "#9499b0", "expense", "desejo")      // OK
        );
        when(repository.findAllByUserUid(USER_UID)).thenReturn(mixed);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(USER_UID);

        // Somente "Saude" deve ser salvo — Moradia e Assinaturas já estão corretos
        verify(repository, times(1)).save(any());
    }

    // ── Cenários: seedDefaults — novo usuário ─────────────────────────

    @Test
    void shouldSeedDefaults_whenUserHasNoCategories() {
        when(repository.findAllByUserUid(USER_UID)).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<CategoryResponse> result = useCase.execute(USER_UID);

        // Deve criar exatamente 10 categorias padrão
        assertThat(result).hasSize(10);
        verify(repository, times(10)).save(any());
    }

    @Test
    void shouldSeedWithCorrectAccentedNames() {
        when(repository.findAllByUserUid(USER_UID)).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<CategoryResponse> result = useCase.execute(USER_UID);

        assertThat(result).extracting(CategoryResponse::name)
                .contains("Alimentação", "Saúde", "Salário/Renda")
                .doesNotContain("Alimentacao", "Saude", "Salario/Renda");
    }

    @Test
    void shouldSeedWithCorrectUserUid() {
        when(repository.findAllByUserUid(USER_UID)).thenReturn(List.of());

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(USER_UID);

        // Todas as categorias seedadas devem pertencer ao usuário correto
        assertThat(captor.getAllValues())
                .extracting(Category::getUserUid)
                .containsOnly(USER_UID);
    }

    @Test
    void shouldSeedWithExpenseIncomeAndBothTypes() {
        when(repository.findAllByUserUid(USER_UID)).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<CategoryResponse> result = useCase.execute(USER_UID);

        // Deve conter os três tipos de categoria
        assertThat(result).extracting(CategoryResponse::type)
                .contains("expense", "income", "both");
    }

    @Test
    void shouldSeedWithNonNullIcons() {
        when(repository.findAllByUserUid(USER_UID)).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<CategoryResponse> result = useCase.execute(USER_UID);

        assertThat(result).extracting(CategoryResponse::icon)
                .doesNotContainNull()
                .allMatch(icon -> !icon.isBlank());
    }
}
