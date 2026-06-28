package com.financeira.api.controller;

import com.financeira.api.service.BotEntryService;
import com.financeira.api.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Webhook que recebe mensagens de serviços WhatsApp (Z-API, Evolution API)
 * e cria lançamentos automaticamente.
 *
 * Endpoints:
 *   POST /api/bot/webhook?token=BOT_TOKEN   — Z-API / Evolution / genérico
 *   POST /api/bot/zapi                      — formato Z-API nativo
 *   POST /api/bot/evolution                 — formato Evolution API nativo
 *   POST /api/bot/test                      — teste manual (autenticado)
 *
 * Para habilitar, configure no Render:
 *   BOT_TOKEN       — token secreto (UUID gerado)
 *   BOT_USER_UID    — Firebase UID do dono do bot
 *   BOT_PHONE       — número autorizado (ex: 5511999999999)
 *
 * Como conectar (Z-API):
 *   URL do webhook: https://financeira-moreira-api.onrender.com/api/bot/webhook?token=SEU_TOKEN
 *   Método: POST
 *   Eventos: Recebimento de mensagens de texto
 */
@RestController
@RequestMapping("/api/bot")
public class BotWebhookController {

    private static final Logger log = LoggerFactory.getLogger(BotWebhookController.class);

    private final BotEntryService botEntryService;
    private final WhatsAppService whatsAppService;

    @Value("${bot.token:}")
    private String botToken;

    @Value("${bot.user-uid:}")
    private String botUserUid;

    @Value("${bot.phone:}")
    private String botPhone;

    public BotWebhookController(BotEntryService botEntryService, WhatsAppService whatsAppService) {
        this.botEntryService = botEntryService;
        this.whatsAppService = whatsAppService;
    }

    // ── Z-API / Evolution / genérico ────────────────────────────────────────
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhook(
            @RequestParam(value = "token", required = false) String token,
            @RequestBody Map<String, Object> payload
    ) {
        if (!isTokenValid(token)) {
            log.warn("[Bot] Token inválido: {}", token);
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "token_invalido"));
        }

        // Tenta extrair phone e message dos formatos conhecidos
        String phone   = extractPhone(payload);
        String message = extractMessage(payload);

        return processMessage(phone, message);
    }

    // ── Z-API formato nativo ─────────────────────────────────────────────────
    @PostMapping("/zapi")
    public ResponseEntity<Map<String, Object>> zapiWebhook(
            @RequestParam(value = "token", required = false) String token,
            @RequestBody Map<String, Object> payload
    ) {
        if (!isTokenValid(token)) return ResponseEntity.status(401).body(Map.of("ok", false));
        // Z-API: { "phone": "5511...", "body": "texto", "type": "ReceivedCallback" }
        String type = String.valueOf(payload.getOrDefault("type", ""));
        if (!"ReceivedCallback".equals(type)) return ResponseEntity.ok(Map.of("ok", true, "skipped", true));
        String phone   = String.valueOf(payload.getOrDefault("phone", ""));
        String message = String.valueOf(payload.getOrDefault("body", ""));
        return processMessage(phone, message);
    }

    // ── Evolution API formato nativo ─────────────────────────────────────────
    @PostMapping("/evolution")
    public ResponseEntity<Map<String, Object>> evolutionWebhook(
            @RequestParam(value = "token", required = false) String token,
            @RequestBody Map<String, Object> payload
    ) {
        if (!isTokenValid(token)) return ResponseEntity.status(401).body(Map.of("ok", false));
        // Evolution: { "event": "messages.upsert", "data": { "key": { "remoteJid": "...", "fromMe": false }, "message": { "conversation": "..." } } }
        String event = String.valueOf(payload.getOrDefault("event", ""));
        if (!"messages.upsert".equals(event)) return ResponseEntity.ok(Map.of("ok", true, "skipped", true));

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        if (data == null) return ResponseEntity.ok(Map.of("ok", false, "error", "sem_data"));

        @SuppressWarnings("unchecked")
        Map<String, Object> key = (Map<String, Object>) data.get("key");
        Boolean fromMe = key != null ? (Boolean) key.getOrDefault("fromMe", false) : false;
        if (Boolean.TRUE.equals(fromMe)) return ResponseEntity.ok(Map.of("ok", true, "skipped", true)); // ignora mensagens enviadas pelo bot

        String remoteJid = key != null ? String.valueOf(key.getOrDefault("remoteJid", "")) : "";
        String phone = remoteJid.replaceAll("@.*", "");

        @SuppressWarnings("unchecked")
        Map<String, Object> msgObj = (Map<String, Object>) data.get("message");
        String message = msgObj != null ? String.valueOf(msgObj.getOrDefault("conversation",
                msgObj.getOrDefault("extendedTextMessage", ""))) : "";

        return processMessage(phone, message);
    }

    // ── Teste manual (autenticado via Firebase) ──────────────────────────────
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testMessage(@RequestBody Map<String, String> body) {
        if (botUserUid.isBlank()) {
            return ResponseEntity.status(503).body(Map.of("ok", false, "error", "BOT_USER_UID nao configurado"));
        }
        String message = body.getOrDefault("message", "gastei 50 no ifood");
        return processMessage(botPhone.isBlank() ? "test" : botPhone, message);
    }

    // ── Status do bot ────────────────────────────────────────────────────────
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "configured", !botToken.isBlank() && !botUserUid.isBlank(),
                "hasToken",   !botToken.isBlank(),
                "hasUserUid", !botUserUid.isBlank(),
                "hasPhone",   !botPhone.isBlank(),
                "webhookUrl", "https://financeira-moreira-api.onrender.com/api/bot/webhook?token=" + (botToken.isBlank() ? "CONFIGURE_BOT_TOKEN" : "[configurado]")
        ));
    }

    // ── Lógica central ───────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> processMessage(String phone, String message) {
        if (message == null || message.isBlank()) {
            return ResponseEntity.ok(Map.of("ok", false, "error", "mensagem_vazia"));
        }

        // Verifica se é do número autorizado (se configurado)
        if (!botPhone.isBlank() && !phone.isBlank() && !phone.equals(botPhone)) {
            log.warn("[Bot] Número não autorizado: {}", phone);
            return ResponseEntity.ok(Map.of("ok", false, "error", "numero_nao_autorizado"));
        }

        if (botUserUid.isBlank()) {
            log.warn("[Bot] BOT_USER_UID não configurado");
            return ResponseEntity.status(503).body(Map.of("ok", false, "error", "bot_nao_configurado"));
        }

        log.info("[Bot] Mensagem de {}: {}", phone, message);

        try {
            BotEntryService.BotResult result = botEntryService.processMessage(botUserUid, message);

            // Envia confirmação via WhatsApp
            if (result.success()) {
                String confirm = "✅ *Lançamento criado!*\n"
                        + "📝 " + result.name() + "\n"
                        + "💰 R$ " + String.format("%.2f", result.amount()).replace(".", ",") + "\n"
                        + "📂 " + result.category() + "\n"
                        + "📅 " + result.kind()
                        + (result.installments() != null ? " — " + result.installments() + "x" : "");
                whatsAppService.enviarAsync(confirm);
            } else {
                whatsAppService.enviarAsync("❌ Não entendi. Tente: *gastei 50 no ifood* ou *recebi 3000 salário*");
            }

            return ResponseEntity.ok(Map.of(
                    "ok",           result.success(),
                    "entryId",      result.entryId() != null ? result.entryId().toString() : "",
                    "name",         result.name() != null ? result.name() : "",
                    "amount",       result.amount() != null ? result.amount() : 0,
                    "kind",         result.kind() != null ? result.kind() : "",
                    "category",     result.category() != null ? result.category() : "",
                    "installments", result.installments() != null ? result.installments() : 0,
                    "error",        result.error() != null ? result.error() : ""
            ));

        } catch (Exception e) {
            log.error("[Bot] Erro ao processar mensagem", e);
            return ResponseEntity.status(500).body(Map.of("ok", false, "error", e.getMessage()));
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private boolean isTokenValid(String token) {
        if (botToken.isBlank()) {
            log.warn("[Bot] BOT_TOKEN não configurado — endpoint inseguro");
            return false;
        }
        return botToken.equals(token);
    }

    @SuppressWarnings("unchecked")
    private String extractPhone(Map<String, Object> payload) {
        // Genérico / Z-API
        if (payload.containsKey("phone")) return String.valueOf(payload.get("phone"));
        // Evolution
        if (payload.containsKey("data")) {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data != null && data.containsKey("key")) {
                Map<String, Object> key = (Map<String, Object>) data.get("key");
                if (key != null) return String.valueOf(key.getOrDefault("remoteJid", "")).replaceAll("@.*", "");
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private String extractMessage(Map<String, Object> payload) {
        // Genérico / Z-API
        if (payload.containsKey("body"))    return String.valueOf(payload.get("body"));
        if (payload.containsKey("message")) return String.valueOf(payload.get("message"));
        if (payload.containsKey("text"))    return String.valueOf(payload.get("text"));
        // Evolution nested
        if (payload.containsKey("data")) {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data != null && data.containsKey("message")) {
                Map<String, Object> msg = (Map<String, Object>) data.get("message");
                if (msg != null) return String.valueOf(msg.getOrDefault("conversation", ""));
            }
        }
        return "";
    }
}
