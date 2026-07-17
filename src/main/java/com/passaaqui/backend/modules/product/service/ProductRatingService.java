package com.passaaqui.backend.modules.product.service;

import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.product.dto.CreateProductRatingDTO;
import com.passaaqui.backend.modules.product.model.ProductModel;
import com.passaaqui.backend.modules.product.model.ProductRatingModel;
import com.passaaqui.backend.modules.product.repository.ProductRatingRepository;
import com.passaaqui.backend.modules.product.repository.ProductRepository;
import com.passaaqui.backend.modules.tourist.repository.TouristRepository;
import com.passaaqui.backend.modules.user.model.UserModel;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductRatingService {

    private final ProductRatingRepository ratingRepository;
    private final ProductRepository productRepository;
    private final TouristRepository touristRepository;

    @Transactional
    public ProductRatingModel rate(Integer productId, Integer userId, CreateProductRatingDTO dto) {
        ProductModel product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        UserModel user = touristRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));

        ProductRatingModel rating = ratingRepository.findByProductIdAndUserId(productId, userId)
                .orElseGet(ProductRatingModel::new);

        rating.setProduct(product);
        rating.setUser(user);
        rating.setRating(dto.rating());

        ratingRepository.save(rating);

        updateProductAverageRating(product);

        return rating;
    }

    private void updateProductAverageRating(ProductModel product) {
        List<ProductRatingModel> ratings = ratingRepository.findByProductId(product.getId());
        double avg = ratings.stream()
                .mapToInt(ProductRatingModel::getRating)
                .average()
                .orElse(0.0);
        product.setAverageRating(avg);
        product.setRatingsCount(ratings.size());
        productRepository.save(product);
    }

    public List<ProductRatingModel> getRatingsByProductId(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found");
        }
        return ratingRepository.findByProductId(productId);
    }
}