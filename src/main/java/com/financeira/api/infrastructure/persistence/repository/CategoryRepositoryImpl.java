package com.financeira.api.infrastructure.persistence.repository;

import com.financeira.api.domain.model.Category;
import com.financeira.api.domain.repository.CategoryRepository;
import com.financeira.api.infrastructure.persistence.entity.CategoryEntity;
import com.financeira.api.infrastructure.persistence.jpa.CategoryJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository jpa;

    public CategoryRepositoryImpl(CategoryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Category save(Category category) {
        return jpa.save(CategoryEntity.fromDomain(category)).toDomain();
    }

    @Override
    public Optional<Category> findByIdAndUserUid(UUID id, String userUid) {
        return jpa.findByIdAndUserUid(id, userUid).map(CategoryEntity::toDomain);
    }

    @Override
    public Optional<Category> findByNameAndUserUid(String name, String userUid) {
        return jpa.findByNameAndUserUid(name, userUid).map(CategoryEntity::toDomain);
    }

    @Override
    public List<Category> findAllByUserUid(String userUid) {
        return jpa.findAllByUserUid(userUid).stream()
                .map(CategoryEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByIdAndUserUid(UUID id, String userUid) {
        jpa.deleteByIdAndUserUid(id, userUid);
    }

    @Override
    public boolean existsByIdAndUserUid(UUID id, String userUid) {
        return jpa.existsByIdAndUserUid(id, userUid);
    }
}
