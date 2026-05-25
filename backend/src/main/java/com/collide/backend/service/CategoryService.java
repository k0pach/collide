package com.collide.backend.service;

import com.collide.backend.dto.CategoryDto;
import com.collide.backend.model.entity.Category;
import com.collide.backend.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final DtoMapper mapper;

    public CategoryService(CategoryRepository categoryRepository, DtoMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> all() {
        return categoryRepository.findAllByOrderBySortOrderAsc().stream().map(mapper::category).toList();
    }

    public Category getBySlugOrNull(String slug) {
        if (slug == null || slug.isBlank() || "all".equals(slug)) return null;
        return categoryRepository.findBySlug(slug).orElse(null);
    }
}
