package com.passaaqui.backend.modules.product.service;

import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.product.dto.CreateProductRatingDTO;
import com.passaaqui.backend.modules.product.dto.ProductRatingResponseDTO;
import com.passaaqui.backend.modules.product.model.ProductModel;
import com.passaaqui.backend.modules.product.model.ProductRatingModel;
import com.passaaqui.backend.modules.product.repository.ProductRatingRepository;
import com.passaaqui.backend.modules.product.repository.ProductRepository;
import com.passaaqui.backend.modules.tourist.repository.TouristRepository;
import com.passaaqui.backend.modules.user.model.UserModel;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductRatingService {

    private final ProductRatingRepository ratingRepository;
    private final ProductRepository productRepository;
    private final TouristRepository touristRepository;
    private final StorageService storageService;

    @Transactional
    public ProductRatingResponseDTO rate(
            Integer productId,
            Integer userId,
            Integer ratingValue,
            String comment,
            String orderId,
            List<MultipartFile> photos,
            MultipartFile video
    ) {
        if (ratingValue == null || ratingValue < 1 || ratingValue > 5) {
            throw new InvalidRequestException("Rating is required and must be between 1 and 5");
        }

        boolean hasPhotos = photos != null && photos.stream().anyMatch(p -> p != null && !p.isEmpty());
        boolean hasVideo = video != null && !video.isEmpty();

        if (!hasPhotos && !hasVideo) {
            throw new InvalidRequestException("At least one image or video is required");
        }

        ProductModel product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        UserModel user = touristRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));

        ProductRatingModel rating = ratingRepository.findByProductIdAndUserId(productId, userId)
                .orElseGet(ProductRatingModel::new);

        rating.setProduct(product);
        rating.setUser(user);
        rating.setRating(ratingValue);
        rating.setComment(comment);
        if (orderId != null && !orderId.isBlank()) {
            rating.setOrderCode(orderId);
        }

        if (photos != null && !photos.isEmpty()) {
            List<String> uploadedImages = new ArrayList<>();
            for (MultipartFile photo : photos) {
                if (photo != null && !photo.isEmpty()) {
                    String storedName = storageService.uploadFile(photo, "ratings/images");
                    uploadedImages.add(storedName);
                }
            }
            if (!uploadedImages.isEmpty()) {
                rating.setImages(uploadedImages);
            }
        }

        if (video != null && !video.isEmpty()) {
            String storedVideo = storageService.uploadFile(video, "ratings/videos");
            rating.setVideo(storedVideo);
        }

        ratingRepository.save(rating);

        updateProductAverageRating(product);

        return toResponseDTO(rating);
    }

    @Transactional
    public ProductRatingModel rate(Integer productId, Integer userId, CreateProductRatingDTO dto) {
        ProductModel product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        UserModel user = touristRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));

        ProductRatingModel rating = ratingRepository.findByProductIdAndUserId(productId, userId)
                .orElseGet(ProductRatingModel::new);

        rating.setProduct(product);
        rating.setUser(user);
        rating.setRating(dto.rating());
        rating.setComment(dto.comment());
        if (dto.orderId() != null && !dto.orderId().isBlank()) {
            rating.setOrderCode(dto.orderId());
        }

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

    public List<ProductRatingResponseDTO> getRatingsByProductId(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found");
        }
        return ratingRepository.findByProductId(productId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public ProductRatingResponseDTO toResponseDTO(ProductRatingModel rating) {
        List<String> photoUrls = null;
        if (rating.getImages() != null && !rating.getImages().isEmpty()) {
            photoUrls = rating.getImages().stream()
                    .map(storageService::getFileUrl)
                    .toList();
        }

        String videoUrl = null;
        if (rating.getVideo() != null && !rating.getVideo().isBlank()) {
            videoUrl = storageService.getFileUrl(rating.getVideo());
        }

        return new ProductRatingResponseDTO(
                rating.getId(),
                rating.getProduct() != null ? rating.getProduct().getId() : null,
                rating.getProduct() != null ? rating.getProduct().getName() : null,
                rating.getOrderCode(),
                rating.getRating(),
                rating.getComment(),
                photoUrls,
                videoUrl,
                rating.getCreatedAt()
        );
    }
}