package com.financeira.api.application.usecase.category;

import com.financeira.api.application.dto.CategoryResponse;
import com.financeira.api.domain.model.Category;
import com.financeira.api.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ListCategoriesUseCase {

    private final CategoryRepository repository;

    /** Mapa de correções de acento para migração de dados existentes. */
    private static final Map<String, String> ACCENT_FIXES = Map.of(
        "Alimentacao",   "Alimentação",
        "Saude",         "Saúde",
        "Salario/Renda", "Salário/Renda",
        "Transferencia", "Transferência"
    );

    public ListCategoriesUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<CategoryResponse> execute(String userUid) {
        List<Category> cats = repository.findAllByUserUid(userUid);
        if (cats.isEmpty()) {
            cats = seedDefaults(userUid);
        } else {
            cats = fixAccents(cats);
        }
        return cats.stream()
                .map(CategoryResponse::from)
                .sorted(Comparator.comparing(r -> r.name().toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Corrige nomes sem acento de seeds anteriores.
     * Idempotente: só salva se houver mudança.
     */
    private List<Category> fixAccents(List<Category> cats) {
        List<Category> result = new ArrayList<>(cats.size());
        for (Category c : cats) {
            String fix = ACCENT_FIXES.get(c.getName());
            if (fix != null) {
                c.setName(fix);
                repository.save(c);
            }
            result.add(c);
        }
        return result;
    }

    /**
     * Cria as categorias padrão para um usuário novo.
     * Espelha SEED_CATEGORIES do frontend (src/data/seed.ts).
     */
    private List<Category> seedDefaults(String userUid) {
        List<Category> defaults = List.of(
            new Category(userUid, "Moradia",        "🏠", new BigDecimal("2500"), "#f87171", "expense", "essencial"),
            new Category(userUid, "Alimentação",    "🍔", new BigDecimal("1200"), "#7c8dff", "expense", "essencial"),
            new Category(userUid, "Transporte",     "🚗", new BigDecimal("800"),  "#fbbf24", "expense", "essencial"),
            new Category(userUid, "Saúde",          "💊", new BigDecimal("400"),  "#34d399", "expense", "essencial"),
            new Category(userUid, "Lazer",          "🎮", new BigDecimal("500"),  "#a78bfa", "expense", "desejo"),
            new Category(userUid, "Assinaturas",    "📺", new BigDecimal("300"),  "#9499b0", "expense", "desejo"),
            new Category(userUid, "Renda",          "💼", BigDecimal.ZERO,        "#34d399", "income",  null),
            new Category(userUid, "Investimentos",  "📈", BigDecimal.ZERO,        "#a78bfa", "income",  "investimento"),
            new Category(userUid, "Outros",         "💳", new BigDecimal("300"),  "#9499b0", "both",    null),
            new Category(userUid, "Salário/Renda",  "💰", BigDecimal.ZERO,        "#22c55e", "income",  null)
        );
        return defaults.stream().map(repository::save).collect(Collectors.toList());
    }
}
