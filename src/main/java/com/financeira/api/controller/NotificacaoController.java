package com.financeira.api.controller;

import com.financeira.api.scheduler.ResumoFinanceiroScheduler;
import com.financeira.api.service.WhatsAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Expoe /api/whatsapp/test e /api/whatsapp/send (portal)
 * + /api/notificacoes/teste e /api/notificacoes/enviar (compatibilidade)
 * + /api/scheduler/resumo para acionar o resumo diário manualmente
 *
 * Usa chamada sincrona para evitar o bug Spring Security + DeferredResult async dispatch
 * (o async dispatch perde o SecurityContext e retorna 401 mesmo com o request autenticado).
 */
@RestController
public class NotificacaoController {

    private final WhatsAppService whatsAppService;
    private final ResumoFinanceiroScheduler resumoScheduler;

    public NotificacaoController(WhatsAppService whatsAppService,
                                 ResumoFinanceiroScheduler resumoScheduler) {
        this.whatsAppService  = whatsAppService;
        this.resumoScheduler  = resumoScheduler;
    }

    @PostMapping("/api/scheduler/resumo")
    public ResponseEntity<Map<String, Object>> acionarResumoDiario() {
        try {
            resumoScheduler.enviarResumoDiario();
            Map<String, Object> resp = new HashMap<>();
            resp.put("ok", true);
            resp.put("message", "Resumo diário acionado com sucesso");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("ok", false);
            resp.put("message", "Erro ao acionar resumo: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    @PostMapping({"/api/whatsapp/test", "/api/notificacoes/teste"})
    public ResponseEntity<Map<String, Object>> test() {
        String status = whatsAppService.enviar("Financeira Moreira - backend online e autenticado!");
        return buildResponse(status, null);
    }

    @PostMapping({"/api/whatsapp/send", "/api/notificacoes/enviar"})
    public ResponseEntity<Map<String, Object>> send(@RequestBody Map<String, String> body) {
        String mensagem = body.getOrDefault("mensagem", "Ola do Financeira Moreira!");
        String status = whatsAppService.enviar(mensagem);
        return buildResponse(status, mensagem);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(String status, String mensagem) {
        boolean ok = "ok".equals(status);
        boolean notConfigured = "nao_configurado".equals(status);
        int httpStatus = ok ? 200 : (notConfigured ? 503 : 502);

        String message;
        if (ok) {
            message = "Mensagem enviada com sucesso!";
        } else if (notConfigured) {
            message = "Configure CALLMEBOT_PHONE e CALLMEBOT_APIKEY no Render";
        } else {
            message = "Erro CallMeBot: " + status;
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("ok", ok);
        resp.put("status", status);
        resp.put("message", message);
        if (mensagem != null) {
            resp.put("mensagem", mensagem);
        }
        return ResponseEntity.status(httpStatus).body(resp);
    }
}
