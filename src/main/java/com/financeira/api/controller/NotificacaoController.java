package com.financeira.api.controller;

import com.financeira.api.service.WhatsAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;

// Mantém /api/notificacoes por compatibilidade + expõe /api/whatsapp que o portal usa
@RestController
public class NotificacaoController {

    private final WhatsAppService whatsAppService;

    public NotificacaoController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    /** POST /api/whatsapp/test — usado pelo portal */
    @PostMapping({"/api/whatsapp/test", "/api/notificacoes/teste"})
    public DeferredResult<ResponseEntity<Map<String, Object>>> test() {
        DeferredResult<ResponseEntity<Map<String, Object>>> result = new DeferredResult<>(20_000L);

        result.onTimeout(() ->
            result.setErrorResult(ResponseEntity.status(504)
                    .body(Map.of("ok", false, "message", "CallMeBot demorou demais (timeout 20s)")))
        );

        whatsAppService.enviarAsync("✅ Financeira Moreira — backend online e configurado!")
                .thenAccept(status -> {
                    boolean ok = "ok".equals(status);
                    int httpStatus = ok ? 200 : ("nao_configurado".equals(status) ? 503 : 502);
                    result.setResult(ResponseEntity.status(httpStatus)
                            .body(Map.of("ok", ok, "status", status,
                                    "message", switch (status) {
                                        case "ok"             -> "Mensagem enviada com sucesso!";
                                        case "nao_configurado"-> "CALLMEBOT_PHONE e CALLMEBOT_APIKEY não configurados no Render";
                                        default               -> "CallMeBot retornou erro: " + status;
                                    })));
                })
                .exceptionally(ex -> {
                    result.setErrorResult(ResponseEntity.status(500)
                            .body(Map.of("ok", false, "message", ex.getMessage())));
                    return null;
                });

        return result;
    }

    /** POST /api/whatsapp/send — usado pelo portal */
    @PostMapping({"/api/whatsapp/send", "/api/notificacoes/enviar"})
    public DeferredResult<ResponseEntity<Map<String, Object>>> send(@RequestBody Map<String, String> body) {
        String mensagem = body.getOrDefault("mensagem", "Olá do Financeira Moreira!");
        DeferredResult<ResponseEntity<Map<String, Object>>> result = new DeferredResult<>(20_000L);

        result.onTimeout(() ->
            result.setErrorResult(ResponseEntity.status(504)
                    .body(Map.of("ok", false, "message", "Timeout após 20s")))
        );

        whatsAppService.enviarAsync(mensagem)
                .thenAccept(status -> {
                    boolean ok = "ok".equals(status);
                    int httpStatus = ok ? 200 : ("nao_configurado".equals(status) ? 503 : 502);
                    result.setResult(ResponseEntity.status(httpStatus)
                            .body(Map.of("ok", ok, "status", status, "mensagem", mensagem,
                                    "message", ok ? "Enviado!" : "nao_configurado".equals(status)
                                            ? "Configure CALLMEBOT_PHONE e CALLMEBOT_APIKEY no Render"
                                            : "Erro ao enviar: " + status)));
                })
                .exceptionally(ex -> {
                    result.setErrorResult(ResponseEntity.status(500)
                            .body(Map.of("ok", false, "message", ex.getMessage())));
                    return null;
                });

        return result;
    }
}
