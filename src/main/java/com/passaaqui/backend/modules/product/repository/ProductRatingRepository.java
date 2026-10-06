package com.passaaqui.backend.modules.product.repository;

import com.passaaqui.backend.modules.product.model.ProductRatingModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRatingRepository extends JpaRepository<ProductRatingModel, Integer> {

    List<ProductRatingModel> findByProductId(Integer productId);

    Optional<ProductRatingModel> findByProductIdAndUserId(Integer productId, Integer userId);

    long countByProductId(Integer productId);
}