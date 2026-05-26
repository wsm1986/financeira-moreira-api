package com.financeira.api.application.usecase.category;

import com.financeira.api.application.dto.CategoryRequest;
import com.financeira.api.application.dto.CategoryResponse;
import com.financeira.api.domain.exception.BusinessException;
import com.financeira.api.domain.exception.ResourceNotFoundException;
import com.financeira.api.domain.model.Category;
import com.financeira.api.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateCategoryUseCase {

    private final CategoryRepository repository;

    public UpdateCategoryUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    public CategoryResponse execute(String userUid, UUID id, CategoryRequest request) {
        Category category = repository.findByIdAndUserUid(id, userUid)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));

        repository.findByNameAndUserUid(request.name(), userUid)
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> { throw new BusinessException("Já existe outra categoria com o nome '" + request.name() + "'"); });

        category.setName(request.name());
        category.setIcon(request.icon());
        category.setBudget(request.budget());
        category.setColor(request.color());
        category.setType(request.type());
        category.setNature(request.nature());

        return CategoryResponse.from(repository.save(category));
    }
}
