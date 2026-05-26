package com.financeira.api.controller;

import com.financeira.api.service.WhatsAppService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final WhatsAppService whatsAppService;

    public NotificacaoController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    @PostMapping("/teste")
    public String teste() {
        return whatsAppService.enviar("✅ Financeira Moreira — backend conectado com sucesso!");
    }
}
