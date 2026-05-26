package com.collide.backend.config;

import com.collide.backend.model.entity.Category;
import com.collide.backend.repository.CategoryRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReferenceDataSeeder implements ApplicationRunner {
    private final CategoryRepository categoryRepository;

    public ReferenceDataSeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedCategories();
    }

    private void seedCategories() {
        Map<String, String> categories = new LinkedHashMap<>();
        categories.put("figures", "Фигурки");
        categories.put("coins", "Монеты");
        categories.put("records", "Пластинки");
        categories.put("books", "Книги");
        categories.put("watches", "Часы");
        categories.put("retro_tech", "Ретро-техника");
        categories.put("table_games", "Настольные игры");
        categories.put("minerals", "Минералы");

        int sortOrder = 10;
        for (Map.Entry<String, String> entry : categories.entrySet()) {
            Category category = categoryRepository.findBySlug(entry.getKey()).orElseGet(Category::new);
            category.setSlug(entry.getKey());
            category.setTitle(entry.getValue());
            category.setSortOrder(sortOrder);
            categoryRepository.save(category);
            sortOrder += 10;
        }
    }
}
