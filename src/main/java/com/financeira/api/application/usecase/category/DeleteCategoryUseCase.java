package com.financeira.api.application.usecase.category;

import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteCategoryUseCase {

    private final CategoryRepository repository;

    public DeleteCategoryUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    public void execute(String userUid, UUID id) {
        if (!repository.existsByIdAndUserUid(id, userUid)) {
            throw new ResourceNotFoundException("Categoria não encontrada");
        }
        repository.deleteByIdAndUserUid(id, userUid);
    }
}
