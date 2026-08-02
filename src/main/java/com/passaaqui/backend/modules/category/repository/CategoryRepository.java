package com.passaaqui.backend.modules.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.passaaqui.backend.modules.category.model.CategoryModel;

public interface CategoryRepository extends JpaRepository<CategoryModel, Integer> {
}
