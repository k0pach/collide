package com.collide.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.collide.backend.dto.CategoryDto;
import com.collide.backend.model.entity.Category;
import com.collide.backend.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DtoMapper mapper;

    private CategoryService service;

    @BeforeEach
    void setUp() {
        service = new CategoryService(categoryRepository, mapper);
    }

    @Test
    void allMapsCategoriesToDtos() {
        Category first = category("books", "Books", 1);
        Category second = category("games", "Games", 2);
        CategoryDto firstDto = new CategoryDto(UUID.randomUUID(), "books", "books", "Books", 1);
        CategoryDto secondDto = new CategoryDto(UUID.randomUUID(), "games", "games", "Games", 2);

        when(categoryRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(first, second));
        when(mapper.category(first)).thenReturn(firstDto);
        when(mapper.category(second)).thenReturn(secondDto);

        assertThat(service.all()).containsExactly(firstDto, secondDto);
    }

    @Test
    void getBySlugOrNullReturnsNullForUnsupportedValues() {
        assertThat(service.getBySlugOrNull(null)).isNull();
        assertThat(service.getBySlugOrNull(" ")).isNull();
        assertThat(service.getBySlugOrNull("all")).isNull();

        verifyNoInteractions(categoryRepository);
    }

    @Test
    void getBySlugOrNullReturnsCategoryWhenFound() {
        Category category = category("books", "Books", 1);
        when(categoryRepository.findBySlug("books")).thenReturn(Optional.of(category));

        assertThat(service.getBySlugOrNull("books")).isSameAs(category);
    }

    @Test
    void getBySlugOrNullReturnsNullWhenCategoryIsMissing() {
        when(categoryRepository.findBySlug("missing")).thenReturn(Optional.empty());

        assertThat(service.getBySlugOrNull("missing")).isNull();
        verify(categoryRepository).findBySlug("missing");
    }

    private Category category(String slug, String title, int sortOrder) {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setSlug(slug);
        category.setTitle(title);
        category.setSortOrder(sortOrder);
        return category;
    }
}
