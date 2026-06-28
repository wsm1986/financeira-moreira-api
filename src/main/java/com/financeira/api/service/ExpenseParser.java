package com.financeira.api.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.*;

/**
 * Parser de linguagem natural (PT-BR) para lançamentos financeiros via WhatsApp.
 *
 * Suporta:
 *   "gastei 50 no ifood"           → débito_avista 50 iFood Alimentação
 *   "paguei 150 gasolina"          → débito_avista 150 Gasolina Transporte
 *   "comprei iPhone em 12x 1200"   → crédito_parcelado 12x 100/mês iPhone
 *   "recebi 5000 salário"          → receita 5000 Salário Renda
 *   "netflix 39,90"                → débito_recorrente 39.90 Netflix Assinaturas
 *   "spotify"                      → débito_recorrente [valor obrigatório]
 *   "50 ifood"                     → débito_avista 50 iFood (forma curta)
 *   "uber 28,50"                   → débito_avista 28.50 Uber Transporte
 */
@Component
public class ExpenseParser {

    // ── Padrão de valor monetário (aceita 1.234,56 e 1234.56 e 1234) ────────
    private static final Pattern AMOUNT_BR  = Pattern.compile(
            "(?:R\\$\\s*)?(?<v>\\d{1,3}(?:\\.\\d{3})*,\\d{1,2}|\\d+[,.]\\d{1,2}|\\d{2,})"
    );

    // ── Parcelamento: "em 12x", "12 vezes", "12x" ───────────────────────────
    private static final Pattern PARCEL_PAT = Pattern.compile(
            "(?:em\\s+)?(\\d+)\\s*[xX]|(?:(\\d+)\\s+(?:vezes|parcelas))"
    );

    // ── Verbos de ação → kind ────────────────────────────────────────────────
    private static final Map<String, String> VERB_KIND = Map.ofEntries(
            Map.entry("recebi",    "receita"),
            Map.entry("recebei",   "receita"),
            Map.entry("entrou",    "receita"),
            Map.entry("ganhei",    "receita"),
            Map.entry("parcelei",  "credito_parcelado"),
            Map.entry("parcelou",  "credito_parcelado"),
            Map.entry("comprei",   "credito_avista"),
            Map.entry("comprou",   "credito_avista"),
            Map.entry("paguei",    "debito_avista"),
            Map.entry("pagou",     "debito_avista"),
            Map.entry("gastei",    "debito_avista"),
            Map.entry("gastou",    "debito_avista"),
            Map.entry("transferi", "transferencia")
    );

    // ── Keywords que indicam receita ─────────────────────────────────────────
    private static final Set<String> INCOME_WORDS = Set.of(
            "salario", "salário", "freelance", "freela", "honorario", "dividendo",
            "aluguel recebido", "renda", "bonus", "bonificacao"
    );

    // ── Keyword → categoria ──────────────────────────────────────────────────
    private static final List<Map.Entry<List<String>, String>> CAT_MAP = List.of(
            Map.entry(List.of("netflix","spotify","prime","disney","hbo","globoplay","deezer",
                    "youtube premium","crunchyroll","apple tv","paramount","mubi"), "Assinaturas"),
            Map.entry(List.of("mercado","supermercado","feira","carrefour","extra","pao de acucar",
                    "atacadao","assai","hortifrutti","hortifruti","atacado","dia","minuto","presente"), "Alimentação"),
            Map.entry(List.of("ifood","rappi","uber eats","mcdonalds","burger","subway","pizza",
                    "restaurante","lanchonete","padaria","acai","cafe","delivery","sushi","japonesa",
                    "churrascaria"), "Alimentação"),
            Map.entry(List.of("uber","99","taxi","combustivel","gasolina","etanol","shell",
                    "ipiranga","posto","pedagio","estacionamento","metrô","metro","onibus",
                    "passagem","transporte"), "Transporte"),
            Map.entry(List.of("farmacia","drogaria","drogas","medico","hospital","clinica",
                    "plano de saude","unimed","amil","bradesco saude","exame","consulta",
                    "odonto","dentista","remedios","medicamento"), "Saúde"),
            Map.entry(List.of("academia","smart fit","bluefit","bodytech","crossfit",
                    "natacao","pilates","yoga"), "Saúde"),
            Map.entry(List.of("aluguel","condominio","iptu","luz","energia","agua","sabesp",
                    "enel","copel","celpe","cpfl","gas","gas encanado"), "Moradia"),
            Map.entry(List.of("internet","net","vivo","tim","claro","oi","nextel","celular",
                    "telefone","operadora"), "Comunicação"),
            Map.entry(List.of("escola","faculdade","curso","livro","material","mensalidade",
                    "universidade","alura","udemy","coursera","duolingo"), "Educação"),
            Map.entry(List.of("cinema","teatro","show","ingresso","parque","viagem","hotel",
                    "airbnb","passagem aerea"), "Lazer"),
            Map.entry(List.of("amazon","mercado livre","shopee","americanas","magazine",
                    "casas bahia","renner","zara","hm","riachuelo","shein","aliexpress"), "Compras"),
            Map.entry(List.of("salario","salário","freelance","freela","bonus","renda",
                    "dividendo","aluguel recebido"), "Renda")
    );

    // ── Preposições e stopwords para limpeza do nome ─────────────────────────
    private static final Set<String> STOP_WORDS = Set.of(
            "no", "na", "nos", "nas", "de", "do", "da", "dos", "das",
            "em", "ao", "aos", "as", "a", "o", "os", "um", "uma",
            "pro", "pra", "para", "com", "que", "e", "ou"
    );

    // ── API pública ──────────────────────────────────────────────────────────
    public record ParsedExpense(
            BigDecimal amount,
            String name,
            String kind,
            String category,
            Integer installments,
            boolean success,
            String errorMessage
    ) {}

    public ParsedExpense parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return fail("Mensagem vazia");
        }

        String text      = rawText.trim();
        String normalized = normalize(text);

        // 1. Detectar parcelamento ("em 12x")
        Integer installments = null;
        Matcher pm = PARCEL_PAT.matcher(normalized);
        if (pm.find()) {
            String n = pm.group(1) != null ? pm.group(1) : pm.group(2);
            if (n != null) installments = Integer.parseInt(n);
        }

        // 2. Detectar kind pelo verbo inicial
        String kind = detectKind(normalized, installments != null);

        // 3. Extrair valor
        BigDecimal amount = extractAmount(normalized);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return fail("Valor não encontrado. Tente: 'gastei 50 no iFood'");
        }

        // 4. Ajustar kind se income keyword
        if (INCOME_WORDS.stream().anyMatch(kw -> normalized.contains(normalize(kw)))) {
            kind = "receita";
        }

        // 5. Extrair nome (remove verbo, valor, preposições, info de parcela)
        String name = extractName(text, normalized, amount, installments);
        if (name.isBlank()) name = "Lançamento via Bot";

        // 6. Capitalizar
        name = capitalize(name);

        // 7. Classificar categoria
        String category = classifyCategory(normalized, name);
        if ("receita".equals(kind)) category = "Renda";

        // 8. Para parcelado, amount é o valor TOTAL; dividir por installments para o mensal
        if ("credito_parcelado".equals(kind) && installments != null && installments > 1) {
            amount = amount.divide(BigDecimal.valueOf(installments), 2, java.math.RoundingMode.HALF_UP);
        }

        return new ParsedExpense(amount, name, kind, category, installments, true, null);
    }

    // ── Helpers privados ─────────────────────────────────────────────────────
    private String detectKind(String normalized, boolean hasInstallment) {
        if (hasInstallment) return "credito_parcelado";
        for (Map.Entry<String, String> e : VERB_KIND.entrySet()) {
            if (normalized.startsWith(e.getKey() + " ") || normalized.equals(e.getKey())) {
                return e.getValue();
            }
        }
        // Palavras que indicam crédito (compras sem verbo)
        if (normalized.contains("credito") || normalized.contains("crédito")) return "credito_avista";
        // Default: débito avista
        return "debito_avista";
    }

    private BigDecimal extractAmount(String normalized) {
        Matcher m = AMOUNT_BR.matcher(normalized);
        BigDecimal best = null;
        while (m.find()) {
            String v = m.group("v").replace(".", "").replace(",", ".");
            try {
                BigDecimal candidate = new BigDecimal(v);
                // Prefere valores maiores (evita capturar "12x" como valor)
                if (best == null || candidate.compareTo(best) > 0) best = candidate;
            } catch (NumberFormatException ignored) {}
        }
        return best;
    }

    private String extractName(String original, String normalized, BigDecimal amount, Integer installments) {
        String work = normalized;

        // Remove verbo inicial
        for (String verb : VERB_KIND.keySet()) {
            if (work.startsWith(verb + " ")) { work = work.substring(verb.length()).trim(); break; }
        }

        // Remove padrão de parcelamento
        work = PARCEL_PAT.matcher(work).replaceAll("").trim();

        // Remove valor monetário
        String amountStr = amount.toPlainString().replace(".", ",");
        work = work.replace("r$", "").replace(amountStr, "").trim();
        // Também remove a representação original (ex: "1.234,56")
        Matcher am = AMOUNT_BR.matcher(work);
        work = am.replaceAll("").trim();

        // Remove stopwords iniciais
        String[] parts = work.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!STOP_WORDS.contains(p) && !p.isBlank()) sb.append(p).append(" ");
        }
        return sb.toString().trim();
    }

    private String classifyCategory(String normalized, String name) {
        String combined = normalized + " " + normalize(name);
        for (Map.Entry<List<String>, String> entry : CAT_MAP) {
            for (String kw : entry.getKey()) {
                if (combined.contains(normalize(kw))) return entry.getValue();
            }
        }
        return "Outros";
    }

    private String normalize(String text) {
        return Normalizer.normalize(text.toLowerCase().trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private String capitalize(String text) {
        if (text.isBlank()) return text;
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    private ParsedExpense fail(String msg) {
        return new ParsedExpense(null, null, null, null, null, false, msg);
    }
}
