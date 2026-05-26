package com.financeira.api.controller;

import com.financeira.api.service.WhatsAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final WhatsAppService whatsAppService;

    public NotificacaoController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    /**
     * Endpoint assíncrono — retorna imediatamente enquanto o WhatsApp é enviado em background.
     * DeferredResult = servlet thread liberada imediatamente; resposta enviada quando CompletableFuture resolve.
     */
    @PostMapping("/teste")
    public DeferredResult<ResponseEntity<Map<String, String>>> teste() {
        DeferredResult<ResponseEntity<Map<String, String>>> result = new DeferredResult<>(20_000L);

        result.onTimeout(() ->
            result.setErrorResult(ResponseEntity.status(504)
                    .body(Map.of("status", "timeout", "mensagem", "CallMeBot demorou demais")))
        );

        whatsAppService.enviarAsync("✅ Financeira Moreira — backend online!")
                .thenAccept(status ->
                    result.setResult(ResponseEntity.ok(Map.of("status", status, "mensagem", "WhatsApp enviado")))
                )
                .exceptionally(ex -> {
                    result.setErrorResult(ResponseEntity.status(500)
                            .body(Map.of("status", "erro", "mensagem", ex.getMessage())));
                    return null;
                });

        return result;
    }

    @PostMapping("/enviar")
    public DeferredResult<ResponseEntity<Map<String, String>>> enviar(@RequestBody Map<String, String> body) {
        String mensagem = body.getOrDefault("mensagem", "Olá do Financeira Moreira!");
        DeferredResult<ResponseEntity<Map<String, String>>> result = new DeferredResult<>(20_000L);

        result.onTimeout(() ->
            result.setErrorResult(ResponseEntity.status(504)
                    .body(Map.of("status", "timeout")))
        );

        whatsAppService.enviarAsync(mensagem)
                .thenAccept(status ->
                    result.setResult(ResponseEntity.ok(Map.of("status", status, "mensagem", mensagem)))
                )
                .exceptionally(ex -> {
                    result.setErrorResult(ResponseEntity.status(500)
                            .body(Map.of("status", "erro", "detalhe", ex.getMessage())));
                    return null;
                });

        return result;
    }
}
