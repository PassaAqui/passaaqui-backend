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

        Object[][] categories = {
            {"Alimentação", "Restaurantes, lanchonetes e food trucks", 1.0},
            {"Mercado", "Supermercados, hortifrútis e açougues", 0.8},
            {"Farmácia", "Farmácias e drogarias", 1.2},
            {"Padaria", "Padarias e confeitarias", 0.9},
            {"Pet Shop", "Produtos e serviços para animais de estimação", 1.0},
            {"Academia", "Academias e estúdios de ginástica", 1.1},
            {"Beleza", "Salões de beleza, barbearias e estéticas", 1.0},
            {"Oficina", "Oficinas mecânicas e autopeças", 0.7},
        };

        for (Object[] cat : categories) {
            CategoryModel category = new CategoryModel();
            category.setName((String) cat[0]);
            category.setDescription((String) cat[1]);
            category.setCategoryWeight((Double) cat[2]);
            repository.save(category);
        }
    }

    @Transactional
    public CategoryModel create(CreateCategoryDTO dto) {
        CategoryModel category = new CategoryModel();
        category.setName(dto.name());
        category.setDescription(dto.description());
        category.setCategoryWeight(dto.categoryWeight());
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
        if (dto.categoryWeight() != null) category.setCategoryWeight(dto.categoryWeight());
        return repository.save(category);
    }

    @Transactional
    public void delete(Integer id) {
        CategoryModel category = findById(id);
        repository.delete(category);
    }
}
