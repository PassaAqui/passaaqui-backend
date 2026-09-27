package com.passaaqui.backend.unit.service;

import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.product.dto.ProductRatingResponseDTO;
import com.passaaqui.backend.modules.product.model.ProductModel;
import com.passaaqui.backend.modules.product.model.ProductRatingModel;
import com.passaaqui.backend.modules.product.repository.ProductRatingRepository;
import com.passaaqui.backend.modules.product.repository.ProductRepository;
import com.passaaqui.backend.modules.product.service.ProductRatingService;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import com.passaaqui.backend.modules.tourist.repository.TouristRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRatingServiceTest {

    @Mock
    private ProductRatingRepository ratingRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TouristRepository touristRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ProductRatingService productRatingService;

    private ProductModel product;
    private TouristModel tourist;
    private MockMultipartFile photo;
    private MockMultipartFile video;

    @BeforeEach
    void setUp() {
        product = new ProductModel();
        product.setId(1);
        product.setName("Tapioca Clássica");

        tourist = new TouristModel();
        tourist.setId(1);

        photo = new MockMultipartFile("photos", "tapioca.jpg", "image/jpeg", "image-content".getBytes());
        video = new MockMultipartFile("video", "review.mp4", "video/mp4", "video-content".getBytes());
    }

    @Test
    void rate_shouldSaveRatingAndReturnDTO_whenPhotoProvided() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(touristRepository.findById(1)).thenReturn(Optional.of(tourist));
        when(ratingRepository.findByProductIdAndUserId(1, 1)).thenReturn(Optional.empty());
        when(storageService.uploadFile(eq(photo), eq("ratings/images"))).thenReturn("stored-image.jpg");
        when(storageService.getFileUrl("stored-image.jpg")).thenReturn("http://minio/stored-image.jpg");
        when(ratingRepository.save(any(ProductRatingModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ratingRepository.findByProductId(1)).thenReturn(List.of());

        ProductRatingResponseDTO response = productRatingService.rate(
                1,
                1,
                5,
                "Excelente comida!",
                "#A3F92",
                List.of(photo),
                null
        );

        assertNotNull(response);
        assertEquals(Integer.valueOf(1), response.productId());
        assertEquals("Tapioca Clássica", response.productName());
        assertEquals("#A3F92", response.orderId());
        assertEquals(5, response.rating());
        assertEquals("Excelente comida!", response.comment());
        assertNotNull(response.photos());
        assertEquals(1, response.photos().size());
        assertEquals("http://minio/stored-image.jpg", response.photos().get(0));
        assertNull(response.video());

        verify(ratingRepository).save(any(ProductRatingModel.class));
        verify(productRepository).save(product);
    }

    @Test
    void rate_shouldSaveRatingAndReturnDTO_whenVideoProvided() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(touristRepository.findById(1)).thenReturn(Optional.of(tourist));
        when(ratingRepository.findByProductIdAndUserId(1, 1)).thenReturn(Optional.empty());
        when(storageService.uploadFile(eq(video), eq("ratings/videos"))).thenReturn("stored-video.mp4");
        when(storageService.getFileUrl("stored-video.mp4")).thenReturn("http://minio/stored-video.mp4");
        when(ratingRepository.save(any(ProductRatingModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ratingRepository.findByProductId(1)).thenReturn(List.of());

        ProductRatingResponseDTO response = productRatingService.rate(
                1,
                1,
                4,
                null,
                "#B7C21",
                null,
                video
        );

        assertNotNull(response);
        assertEquals(Integer.valueOf(1), response.productId());
        assertEquals(4, response.rating());
        assertNull(response.comment());
        assertNull(response.photos());
        assertEquals("http://minio/stored-video.mp4", response.video());
    }

    @Test
    void rate_shouldThrowInvalidRequestException_whenNoPhotoAndNoVideoProvided() {
        assertThrows(InvalidRequestException.class, () -> productRatingService.rate(
                1,
                1,
                5,
                "Sem fotos ou vídeos",
                "#A3F92",
                null,
                null
        ));

        assertThrows(InvalidRequestException.class, () -> productRatingService.rate(
                1,
                1,
                5,
                "Com fotos vazias",
                "#A3F92",
                List.of(new MockMultipartFile("photos", "", "image/jpeg", new byte[0])),
                null
        ));
    }

    @Test
    void rate_shouldThrowInvalidRequestException_whenRatingOutOfRange() {
        assertThrows(InvalidRequestException.class, () -> productRatingService.rate(
                1,
                1,
                0,
                "Nota zero",
                "#A3F92",
                List.of(photo),
                null
        ));

        assertThrows(InvalidRequestException.class, () -> productRatingService.rate(
                1,
                1,
                6,
                "Nota seis",
                "#A3F92",
                List.of(photo),
                null
        ));
    }

    @Test
    void rate_shouldThrowResourceNotFoundException_whenProductNotFound() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productRatingService.rate(
                999,
                1,
                5,
                "Comentário",
                "#A3F92",
                List.of(photo),
                null
        ));
    }

    @Test
    void rate_shouldThrowResourceNotFoundException_whenTouristNotFound() {
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(touristRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productRatingService.rate(
                1,
                999,
                5,
                "Comentário",
                "#A3F92",
                List.of(photo),
                null
        ));
    }

    @Test
    void getRatingsByProductId_shouldReturnList() {
        when(productRepository.existsById(1)).thenReturn(true);
        ProductRatingModel rating = new ProductRatingModel();
        rating.setId(10);
        rating.setProduct(product);
        rating.setRating(5);
        rating.setImages(List.of("img1.jpg"));
        when(ratingRepository.findByProductId(1)).thenReturn(List.of(rating));
        when(storageService.getFileUrl("img1.jpg")).thenReturn("http://minio/img1.jpg");

        List<ProductRatingResponseDTO> ratings = productRatingService.getRatingsByProductId(1);

        assertNotNull(ratings);
        assertEquals(1, ratings.size());
        assertEquals(5, ratings.get(0).rating());
        assertEquals("http://minio/img1.jpg", ratings.get(0).photos().get(0));
    }

    @Test
    void getRatingsByProductId_shouldThrow_whenProductNotFound() {
        when(productRepository.existsById(999)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productRatingService.getRatingsByProductId(999));
    }
}
