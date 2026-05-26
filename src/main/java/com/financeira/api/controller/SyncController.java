package com.financeira.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SyncController {

    @GetMapping("/sync")
    public String sync() {
        return "Sincronizado";
    }

    @GetMapping("/version")
    public String version() {
        return "build:b4f0fbd whatsapp-sync+cat-autoseed";
    }
}
