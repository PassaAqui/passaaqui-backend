package com.passaaqui.backend.modules.product.repository;

import com.passaaqui.backend.modules.product.model.ProductModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductModel, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductModel p WHERE p.id = :id")
    Optional<ProductModel> findByIdForUpdate(@Param("id") Integer id);

    Page<ProductModel> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<ProductModel> findByPoiId(Integer poiId);

    Page<ProductModel> findByCategoryId(Integer categoryId, Pageable pageable);

    List<ProductModel> findByShopkeeperId(Integer shopkeeperId);

    List<ProductModel> findByShopkeeperIdAndStockGreaterThan(Integer shopkeeperId, Integer stock);

    long countByShopkeeperId(Integer shopkeeperId);

    long countByShopkeeperIdAndActiveTrue(Integer shopkeeperId);

    long countByShopkeeperIdAndHighlightTrue(Integer shopkeeperId);
}
