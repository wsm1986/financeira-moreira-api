package com.financeira.api.loader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeira.api.domain.model.Category;
import com.financeira.api.domain.model.User;
import com.financeira.api.domain.repository.CategoryRepository;
import com.financeira.api.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * Lê /resources/seed-data.json (export do localStorage folego-finance-v2)
 * e semeia o H2 no perfil dev.
 *
 * Estrutura esperada:
 * { "state": { "categories": [...], ... }, "version": N }
 */
@Component
@Profile("dev")
public class DataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper mapper;

    public DataLoader(CategoryRepository categoryRepository,
                      UserRepository userRepository,
                      ObjectMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.userRepository     = userRepository;
        this.mapper             = mapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource resource = new ClassPathResource("seed-data.json");
        if (!resource.exists()) {
            log.info("seed-data.json não encontrado — pulando seed");
            return;
        }

        SeedFile seed;
        try (InputStream is = resource.getInputStream()) {
            seed = mapper.readValue(is, SeedFile.class);
        }

        if (seed.state() == null) {
            log.warn("seed-data.json sem campo 'state' — pulando seed");
            return;
        }

        // Usuário padrão de seed (dev)
        User devUser = new User("dev-user-001", "dev@financeira.local", "Dev User");
        userRepository.save(devUser);

        // Categorias
        List<CategoryJson> cats = seed.state().categories();
        if (cats != null) {
            int count = 0;
            for (CategoryJson c : cats) {
                if (categoryRepository.findByNameAndUserUid(c.name(), devUser.getUid()).isPresent()) {
                    continue;
                }
                BigDecimal budget = c.budget() != null
                        ? BigDecimal.valueOf(c.budget())
                        : BigDecimal.ZERO;
                Category cat = new Category(devUser.getUid(), c.name(), c.icon(), budget,
                        c.color(), c.type(), c.nature());
                categoryRepository.save(cat);
                count++;
            }
            log.info("DataLoader: {} categoria(s) importada(s)", count);
        }
    }

    // ─── DTOs de deserialização ─────────────────────────────────────────

    record SeedFile(SeedState state, Integer version) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SeedState(List<CategoryJson> categories) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CategoryJson(String id, String name, String icon,
                        Double budget, String color,
                        String type, String nature) {}
}
