package com.passaaqui.backend.modules.category.service;

import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.category.dto.CreateCategoryDTO;
import com.passaaqui.backend.modules.category.dto.UpdateCategoryDTO;
import com.passaaqui.backend.modules.category.model.CategoryModel;
import com.passaaqui.backend.modules.category.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;

    @PostConstruct
    @Transactional
    public void seedCategories() {
        if (repository.count() > 0) return;

        String[][] categories = {
            {"Alimentação", "Restaurantes, lanchonetes e food trucks"},
            {"Mercado", "Supermercados, hortifrútis e açougues"},
            {"Farmácia", "Farmácias e drogarias"},
            {"Padaria", "Padarias e confeitarias"},
            {"Pet Shop", "Produtos e serviços para animais de estimação"},
            {"Academia", "Academias e estúdios de ginástica"},
            {"Beleza", "Salões de beleza, barbearias e estéticas"},
            {"Oficina", "Oficinas mecânicas e autopeças"},
        };

        for (String[] cat : categories) {
            CategoryModel category = new CategoryModel();
            category.setName(cat[0]);
            category.setDescription(cat[1]);
            repository.save(category);
        }
    }

    @Transactional
    public CategoryModel create(CreateCategoryDTO dto) {
        CategoryModel category = new CategoryModel();
        category.setName(dto.name());
        category.setDescription(dto.description());
        return repository.save(category);
    }

    public List<CategoryModel> findAll() {
        return repository.findAll();
    }

    public CategoryModel findById(Integer id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Transactional
    public CategoryModel update(Integer id, UpdateCategoryDTO dto) {
        CategoryModel category = findById(id);
        if (dto.name() != null && !dto.name().isBlank()) category.setName(dto.name());
        if (dto.description() != null && !dto.description().isBlank()) category.setDescription(dto.description());
        return repository.save(category);
    }

    @Transactional
    public void delete(Integer id) {
        CategoryModel category = findById(id);
        repository.delete(category);
    }
}
