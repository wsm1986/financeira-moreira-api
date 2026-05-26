package com.financeira.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Inicializa o Firebase Admin SDK no perfil prod.
 *
 * Prioridade de credenciais:
 *   1. Env var FIREBASE_CREDENTIALS_JSON  — JSON completo da service account
 *   2. Env var GOOGLE_APPLICATION_CREDENTIALS — path para o arquivo JSON (padrão Google)
 *   3. Application Default Credentials (ADC) — detectado automaticamente (GCP, Cloud Run etc.)
 *
 * Se nenhuma credencial estiver disponível, apenas loga aviso e continua.
 * O FirebaseAuthFilter retornará 401 para tokens, mas o app sobe normalmente.
 */
@Configuration
@Profile("prod")
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.project-id:}")
    private String projectId;

    // JSON completo da service account como string (configurar no Render como env var)
    @Value("${FIREBASE_CREDENTIALS_JSON:}")
    private String credentialsJson;

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("FirebaseApp já inicializado, pulando");
            return;
        }

        try {
            GoogleCredentials credentials = resolveCredentials();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId.isBlank() ? null : projectId)
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase inicializado — projeto: {}", projectId.isBlank() ? "(ADC)" : projectId);

        } catch (Exception e) {
            // Não quebra o startup — endpoints públicos continuam funcionando
            // Endpoints autenticados retornarão 401 até as credenciais serem configuradas
            log.warn("Firebase NÃO inicializado: {}. Endpoints autenticados retornarão 401.", e.getMessage());
        }
    }

    private GoogleCredentials resolveCredentials() throws IOException {
        // 1. JSON inline (Render secret env var)
        if (!credentialsJson.isBlank()) {
            log.debug("Usando FIREBASE_CREDENTIALS_JSON");
            InputStream stream = new ByteArrayInputStream(
                    credentialsJson.getBytes(StandardCharsets.UTF_8));
            return GoogleCredentials.fromStream(stream);
        }

        // 2 & 3. GOOGLE_APPLICATION_CREDENTIALS path ou ADC (detectado automaticamente)
        log.debug("Usando Application Default Credentials (GOOGLE_APPLICATION_CREDENTIALS ou ADC)");
        return GoogleCredentials.getApplicationDefault();
    }
}
