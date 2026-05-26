package com.financeira.api.application.usecase.category;

import com.financeira.api.application.dto.CategoryRequest;
import com.financeira.api.application.dto.CategoryResponse;
import com.financeira.api.domain.exception.BusinessException;
import com.financeira.api.domain.model.Category;
import com.financeira.api.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateCategoryUseCase {

    private final CategoryRepository repository;

    public CreateCategoryUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    public CategoryResponse execute(String userUid, CategoryRequest request) {
        if (repository.findByNameAndUserUid(request.name(), userUid).isPresent()) {
            throw new BusinessException("Categoria '" + request.name() + "' já existe");
        }
        Category category = new Category(
                userUid,
                request.name(),
                request.icon(),
                request.budget(),
                request.color(),
                request.type(),
                request.nature()
        );
        return CategoryResponse.from(repository.save(category));
    }
}
