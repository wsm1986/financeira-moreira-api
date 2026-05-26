package com.financeira.api.domain.repository;

import com.financeira.api.domain.model.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findByIdAndUserUid(UUID id, String userUid);
    Optional<Category> findByNameAndUserUid(String name, String userUid);
    List<Category> findAllByUserUid(String userUid);
    void deleteByIdAndUserUid(UUID id, String userUid);
    boolean existsByIdAndUserUid(UUID id, String userUid);
}
