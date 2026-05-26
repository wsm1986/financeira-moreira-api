package com.financeira.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${callmebot.phone:}")
    private String phone;

    @Value("${callmebot.apikey:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Envia mensagem de forma assíncrona — não bloqueia a thread do caller.
     * Retorna CompletableFuture para o caller optar por aguardar ou encadear (.thenApply etc).
     */
    @Async
    public CompletableFuture<String> enviarAsync(String mensagem) {
        if (phone.isBlank() || apiKey.isBlank()) {
            log.warn("WhatsApp não configurado (CALLMEBOT_PHONE/APIKEY ausentes)");
            return CompletableFuture.completedFuture("não configurado");
        }

        String texto = URLEncoder.encode(mensagem, StandardCharsets.UTF_8);
        String url = "https://api.callmebot.com/whatsapp.php?phone=%s&text=%s&apikey=%s"
                .formatted(phone, texto, apiKey);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> switch (response.statusCode()) {
                    case 200 -> { log.info("WhatsApp enviado: {}", mensagem); yield "ok"; }
                    default  -> { log.warn("WhatsApp status {}: {}", response.statusCode(), response.body()); yield "erro:" + response.statusCode(); }
                })
                .exceptionally(ex -> {
                    log.error("Falha ao enviar WhatsApp", ex);
                    return "erro:" + ex.getMessage();
                });
    }

    // Atalho síncrono quando necessário (ex: testes, alertas críticos)
    public String enviar(String mensagem) {
        return enviarAsync(mensagem).join();
    }
}
