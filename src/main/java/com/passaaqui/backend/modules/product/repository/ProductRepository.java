package com.passaaqui.backend.modules.product.repository;

import com.passaaqui.backend.modules.product.model.ProductModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductModel, Integer> {

    Page<ProductModel> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<ProductModel> findByPoiId(Integer poiId);
}
