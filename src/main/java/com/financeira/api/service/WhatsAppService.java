package com.financeira.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class WhatsAppService {

    @Value("${callmebot.phone}")
    private String phone;

    @Value("${callmebot.apikey}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String enviar(String mensagem) {
        try {
            String texto = URLEncoder.encode(mensagem, StandardCharsets.UTF_8);
            String url = String.format(
                "https://api.callmebot.com/whatsapp.php?phone=%s&text=%s&apikey=%s",
                phone, texto, apiKey
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200 ? "Mensagem enviada com sucesso." : "Erro ao enviar: " + response.body();

        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
    }
}
