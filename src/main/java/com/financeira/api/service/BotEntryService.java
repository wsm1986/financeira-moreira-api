package com.financeira.api.service;

import com.financeira.api.application.dto.EntryRequest;
import com.financeira.api.application.usecase.entry.CreateEntryUseCase;
import com.financeira.api.domain.model.Category;
import com.financeira.api.domain.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquestra o fluxo:
 *   mensagem WhatsApp → ExpenseParser → busca categoria → CreateEntryUseCase
 */
@Service
public class BotEntryService {

    private static final Logger log = LoggerFactory.getLogger(BotEntryService.class);

    private final ExpenseParser       parser;
    private final CategoryRepository  categoryRepo;
    private final CreateEntryUseCase  createEntry;

    public BotEntryService(ExpenseParser parser, CategoryRepository categoryRepo, CreateEntryUseCase createEntry) {
        this.parser       = parser;
        this.categoryRepo = categoryRepo;
        this.createEntry  = createEntry;
    }

    public record BotResult(
            boolean  success,
            UUID     entryId,
            String   name,
            BigDecimal amount,
            String   kind,
            String   category,
            Integer  installments,
            String   error
    ) {}

    public BotResult processMessage(String userUid, String rawText) {
        // 1. Parsear
        ExpenseParser.ParsedExpense parsed = parser.parse(rawText);
        if (!parsed.success()) {
            log.warn("[Bot] Parse falhou: {} — entrada: {}", parsed.errorMessage(), rawText);
            return new BotResult(false, null, null, null, null, null, null, parsed.errorMessage());
        }

        // 2. Resolver categoria
        String monthKey = YearMonth.now().toString(); // ex: "2026-06"
        UUID categoryId = resolveCategory(userUid, parsed.category(), parsed.kind());
        if (categoryId == null) {
            // Cria categoria default se não existir nenhuma
            log.warn("[Bot] Categoria '{}' não encontrada para uid={}", parsed.category(), userUid);
            return new BotResult(false, null, parsed.name(), parsed.amount(),
                    parsed.kind(), parsed.category(), null,
                    "Categoria '" + parsed.category() + "' não encontrada. Crie ela no app primeiro.");
        }

        // 3. Montar ícone automático
        String icon = resolveIcon(parsed.kind(), parsed.category());

        // 4. Criar lançamento(s)
        try {
            if ("credito_parcelado".equals(parsed.kind()) && parsed.installments() != null && parsed.installments() > 1) {
                return createInstallments(userUid, parsed, categoryId, icon, monthKey);
            } else {
                EntryRequest req = new EntryRequest(
                        monthKey,
                        parsed.kind(),
                        parsed.name(),
                        categoryId,
                        parsed.amount(),
                        LocalDate.now(),
                        icon,
                        null, null, null, null, null, null, null, null, null, null,
                        false, false,
                        "Via WhatsApp Bot",
                        null
                );
                var response = createEntry.execute(userUid, req);
                return new BotResult(true, response.id(), parsed.name(), parsed.amount(),
                        parsed.kind(), parsed.category(), null, null);
            }
        } catch (Exception e) {
            log.error("[Bot] Erro ao criar entry", e);
            return new BotResult(false, null, parsed.name(), parsed.amount(), parsed.kind(),
                    parsed.category(), null, "Erro interno: " + e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private BotResult createInstallments(String userUid, ExpenseParser.ParsedExpense parsed,
                                          UUID categoryId, String icon, String startMk) {
        int total = parsed.installments();
        UUID groupId = UUID.randomUUID();
        UUID firstId = null;

        String mk = startMk;
        for (int i = 1; i <= total; i++) {
            EntryRequest req = new EntryRequest(
                    mk,
                    "credito_parcelado",
                    parsed.name() + " — " + i + "/" + total,
                    categoryId,
                    parsed.amount(), // já é o valor mensal (dividido no parser)
                    LocalDate.now(),
                    icon,
                    null,
                    total,
                    i,
                    groupId,
                    null, null, null, null, null, null,
                    false, false,
                    "Via WhatsApp Bot",
                    null
            );
            var response = createEntry.execute(userUid, req);
            if (i == 1) firstId = response.id();
            mk = nextMonth(mk);
        }

        return new BotResult(true, firstId, parsed.name(),
                parsed.amount().multiply(BigDecimal.valueOf(total)),
                "credito_parcelado", parsed.category(), total, null);
    }

    private UUID resolveCategory(String userUid, String categoryName, String kind) {
        // Tenta nome exato
        Optional<Category> exact = categoryRepo.findByNameAndUserUid(categoryName, userUid);
        if (exact.isPresent()) return exact.get().getId();

        // Tenta busca parcial
        List<Category> all = categoryRepo.findAllByUserUid(userUid);
        String nameLower = categoryName.toLowerCase();
        Optional<Category> partial = all.stream()
                .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(nameLower))
                .findFirst();
        if (partial.isPresent()) return partial.get().getId();

        // Para receita, tenta qualquer categoria de receita
        if ("receita".equals(kind)) {
            Optional<Category> income = all.stream()
                    .filter(c -> c.getType() != null && c.getType().equalsIgnoreCase("income"))
                    .findFirst();
            if (income.isPresent()) return income.get().getId();
        }

        // Fallback: primeira categoria disponível
        return all.isEmpty() ? null : all.get(0).getId();
    }

    private String resolveIcon(String kind, String category) {
        return switch (category) {
            case "Alimentação"  -> "🍕";
            case "Transporte"   -> "🚗";
            case "Moradia"      -> "🏠";
            case "Saúde"        -> "💊";
            case "Educação"     -> "📚";
            case "Lazer"        -> "🎬";
            case "Assinaturas"  -> "📱";
            case "Compras"      -> "🛍️";
            case "Comunicação"  -> "📡";
            case "Renda"        -> "💵";
            default             -> "receita".equals(kind) ? "💵" : "💸";
        };
    }

    private String nextMonth(String mk) {
        YearMonth ym = YearMonth.parse(mk);
        return ym.plusMonths(1).toString();
    }
}
