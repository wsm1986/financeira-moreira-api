package com.financeira.api.controller;

import com.financeira.api.service.WhatsAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Expoe endpoints de WhatsApp em /api/whatsapp/* (usado pelo portal)
 * e mantém /api/notificacoes/* por compatibilidade.
 */
@RestController
public class NotificacaoController {

    private final WhatsAppService whatsAppService;

    public NotificacaoController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    /** POST /api/whatsapp/test e /api/notificacoes/teste */
    @PostMapping({"/api/whatsapp/test", "/api/notificacoes/teste"})
    public DeferredResult<ResponseEntity<Map<String, Object>>> test() {
        DeferredResult<ResponseEntity<Map<String, Object>>> result = new DeferredResult<>(20_000L);

        result.onTimeout(() -> {
            Map<String, Object> body = new HashMap<>();
            body.put("ok", false);
            body.put("message", "CallMeBot demorou demais (timeout 20s)");
            result.setErrorResult(ResponseEntity.status(504).body(body));
        });

        whatsAppService.enviarAsync("Financeira Moreira - backend online!")
                .thenAccept(status -> {
                    boolean ok = "ok".equals(status);
                    boolean notConfigured = "nao_configurado".equals(status);
                    int httpStatus = ok ? 200 : (notConfigured ? 503 : 502);

                    String message;
                    if (ok) {
                        message = "Mensagem enviada com sucesso!";
                    } else if (notConfigured) {
                        message = "Configure CALLMEBOT_PHONE e CALLMEBOT_APIKEY no Render";
                    } else {
                        message = "CallMeBot retornou erro: " + status;
                    }

                    Map<String, Object> body = new HashMap<>();
                    body.put("ok", ok);
                    body.put("status", status);
                    body.put("message", message);
                    result.setResult(ResponseEntity.status(httpStatus).body(body));
                })
                .exceptionally(ex -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("ok", false);
                    body.put("message", ex.getMessage() != null ? ex.getMessage() : "Erro desconhecido");
                    result.setErrorResult(ResponseEntity.status(500).body(body));
                    return null;
                });

        return result;
    }

    /** POST /api/whatsapp/send e /api/notificacoes/enviar */
    @PostMapping({"/api/whatsapp/send", "/api/notificacoes/enviar"})
    public DeferredResult<ResponseEntity<Map<String, Object>>> send(@RequestBody Map<String, String> body) {
        String mensagem = body.getOrDefault("mensagem", "Ola do Financeira Moreira!");
        DeferredResult<ResponseEntity<Map<String, Object>>> result = new DeferredResult<>(20_000L);

        result.onTimeout(() -> {
            Map<String, Object> resp = new HashMap<>();
            resp.put("ok", false);
            resp.put("message", "Timeout apos 20s");
            result.setErrorResult(ResponseEntity.status(504).body(resp));
        });

        whatsAppService.enviarAsync(mensagem)
                .thenAccept(status -> {
                    boolean ok = "ok".equals(status);
                    boolean notConfigured = "nao_configurado".equals(status);
                    int httpStatus = ok ? 200 : (notConfigured ? 503 : 502);

                    String message;
                    if (ok) {
                        message = "Enviado!";
                    } else if (notConfigured) {
                        message = "Configure CALLMEBOT_PHONE e CALLMEBOT_APIKEY no Render";
                    } else {
                        message = "Erro ao enviar: " + status;
                    }

                    Map<String, Object> resp = new HashMap<>();
                    resp.put("ok", ok);
                    resp.put("status", status);
                    resp.put("mensagem", mensagem);
                    resp.put("message", message);
                    result.setResult(ResponseEntity.status(httpStatus).body(resp));
                })
                .exceptionally(ex -> {
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("ok", false);
                    resp.put("message", ex.getMessage() != null ? ex.getMessage() : "Erro desconhecido");
                    result.setErrorResult(ResponseEntity.status(500).body(resp));
                    return null;
                });

        return result;
    }
}
