package com.financeira.api.application.usecase.category;

import com.financeira.api.application.dto.CategoryResponse;
import com.financeira.api.domain.model.Category;
import com.financeira.api.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListCategoriesUseCase {

    private final CategoryRepository repository;

    public ListCategoriesUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<CategoryResponse> execute(String userUid) {
        List<Category> cats = repository.findAllByUserUid(userUid);
        if (cats.isEmpty()) {
            cats = seedDefaults(userUid);
        }
        return cats.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Cria as categorias padrao para um usuario novo.
     * Espelha SEED_CATEGORIES do frontend (src/data/seed.ts).
     */
    private List<Category> seedDefaults(String userUid) {
        List<Category> defaults = List.of(
            new Category(userUid, "Moradia",       "🏠", new BigDecimal("2500"), "#f87171", "expense", "essencial"),
            new Category(userUid, "Alimentacao",   "🍔", new BigDecimal("1200"), "#7c8dff", "expense", "essencial"),
            new Category(userUid, "Transporte",    "🚗", new BigDecimal("800"),  "#fbbf24", "expense", "essencial"),
            new Category(userUid, "Saude",         "💊", new BigDecimal("400"),  "#34d399", "expense", "essencial"),
            new Category(userUid, "Lazer",         "🎮", new BigDecimal("500"),  "#a78bfa", "expense", "desejo"),
            new Category(userUid, "Assinaturas",   "📺", new BigDecimal("300"),  "#9499b0", "expense", "desejo"),
            new Category(userUid, "Renda",         "💼", BigDecimal.ZERO,        "#34d399", "income",  null),
            new Category(userUid, "Investimentos", "📈", BigDecimal.ZERO,        "#a78bfa", "income",  "investimento"),
            new Category(userUid, "Outros",        "💳", new BigDecimal("300"),  "#9499b0", "both",    null),
            new Category(userUid, "Salario/Renda", "💰", BigDecimal.ZERO,        "#22c55e", "income",  null)
        );
        return defaults.stream().map(repository::save).collect(Collectors.toList());
    }
}
