package com.financeira.api.infrastructure.web;

import com.financeira.api.application.dto.CategoryRequest;
import com.financeira.api.application.dto.CategoryResponse;
import com.financeira.api.application.usecase.category.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CreateCategoryUseCase create;
    private final UpdateCategoryUseCase update;
    private final DeleteCategoryUseCase delete;
    private final ListCategoriesUseCase list;

    public CategoryController(CreateCategoryUseCase create,
                               UpdateCategoryUseCase update,
                               DeleteCategoryUseCase delete,
                               ListCategoriesUseCase list) {
        this.create = create;
        this.update = update;
        this.delete = delete;
        this.list   = list;
    }

    @GetMapping
    public List<CategoryResponse> listAll(Authentication auth) {
        return list.execute(uid(auth));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request,
                                                    Authentication auth) {
        CategoryResponse response = create.execute(uid(auth), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id,
                                    @Valid @RequestBody CategoryRequest request,
                                    Authentication auth) {
        return update.execute(uid(auth), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication auth) {
        delete.execute(uid(auth), id);
        return ResponseEntity.noContent().build();
    }

    private String uid(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getUsername();
        }
        return String.valueOf(principal);
    }
}
