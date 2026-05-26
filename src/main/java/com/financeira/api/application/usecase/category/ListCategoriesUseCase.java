package com.financeira.api.application.usecase.category;

import com.financeira.api.application.dto.CategoryResponse;
import com.financeira.api.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListCategoriesUseCase {

    private final CategoryRepository repository;

    public ListCategoriesUseCase(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<CategoryResponse> execute(String userUid) {
        return repository.findAllByUserUid(userUid).stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }
}
