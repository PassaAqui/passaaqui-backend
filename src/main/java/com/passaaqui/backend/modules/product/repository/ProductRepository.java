package com.passaaqui.backend.modules.product.repository;

import com.passaaqui.backend.modules.product.model.ProductModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductModel, Integer> {

    Page<ProductModel> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<ProductModel> findByPoiId(Integer poiId);

    Page<ProductModel> findByCategoryId(Integer categoryId, Pageable pageable);

    List<ProductModel> findByShopkeeperId(Integer shopkeeperId);

    List<ProductModel> findByShopkeeperIdAndStockGreaterThan(Integer shopkeeperId, Integer stock);

    long countByShopkeeperId(Integer shopkeeperId);

    long countByShopkeeperIdAndActiveTrue(Integer shopkeeperId);

    long countByShopkeeperIdAndHighlightTrue(Integer shopkeeperId);
}
