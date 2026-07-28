package com.passaaqui.backend.modules.product.service;

import com.passaaqui.backend.infra.exception.ForbiddenException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.category.model.CategoryModel;
import com.passaaqui.backend.modules.category.repository.CategoryRepository;
import com.passaaqui.backend.modules.order.repository.OrderRepository;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.repository.PoiRepository;
import com.passaaqui.backend.modules.product.dto.CatalogMetricsDTO;
import com.passaaqui.backend.modules.product.dto.CreateProductDTO;
import com.passaaqui.backend.modules.product.dto.UpdateProductDTO;
import com.passaaqui.backend.modules.product.model.ProductModel;
import com.passaaqui.backend.modules.product.repository.ProductRepository;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import com.passaaqui.backend.modules.shopkeeper.repository.ShopkeeperRepository;
import com.passaaqui.backend.infra.exception.InvalidRequestException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final ShopkeeperRepository shopkeeperRepository;
    private final CategoryRepository categoryRepository;
    private final PoiRepository poiRepository;
    private final OrderRepository orderRepository;
    private final XpCalculationStrategy xpCalculationStrategy;
    private final StorageService storageService;

    @Transactional
    public ProductModel create(CreateProductDTO dto) {
        ShopkeeperModel shopkeeper = shopkeeperRepository.findById(dto.shopkeeperId())
            .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found"));

        CategoryModel category = categoryRepository.findById(dto.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        PoiModel poi = poiRepository.findById(dto.poiId())
            .orElseThrow(() -> new ResourceNotFoundException("POI not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(a -> a.startsWith("ROLE_ADMIN"));

        if (!isAdmin && (poi.getShopkeeper() == null || !poi.getShopkeeper().getId().equals(shopkeeper.getId()))) {
            throw new ForbiddenException("You can only create products for your own POI");
        }

        ProductModel product = new ProductModel();
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());

        Integer maxXp = resolveMaxXp(dto, category);
        product.setMaxXp(maxXp);

        product.setShopkeeper(shopkeeper);
        product.setPoi(poi);
        product.setCategory(category);
        product.setStock(dto.stock() != null ? dto.stock() : 0);
        product.setActive(dto.active() != null ? dto.active() : true);
        product.setHighlight(dto.highlight() != null ? dto.highlight() : false);

        return repository.save(product);
    }

    private Integer resolveMaxXp(CreateProductDTO dto, CategoryModel category) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(a -> a.startsWith("ROLE_ADMIN"));

        if (isAdmin && dto.maxXp() != null) {
            return dto.maxXp();
        }

        return xpCalculationStrategy.calculate(dto.price(), category.getCategoryWeight());
    }

    public ProductModel findById(Integer id) {
        ProductModel product = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        enrichImageUrls(product);
        return product;
    }

    public ProductModel findByIdWithAccessCheck(Integer id) {
        ProductModel product = findById(id);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getPrincipal().toString();
        var authorities = auth.getAuthorities();

        boolean isAdmin = authorities != null && authorities.stream()
                .anyMatch(a -> a.getAuthority().startsWith("ROLE_ADMIN"));

        if (isAdmin) {
            return product;
        }

        Integer currentUserId = Integer.parseInt(userId);
        boolean isShopkeeper = product.getShopkeeper().getId().equals(currentUserId);

        if (isShopkeeper) {
            return product;
        }

        boolean isTourist = authorities != null && authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TOURIST"));

        if (isTourist && orderRepository.existsByTourist_IdAndProduct_Id(currentUserId, id)) {
            return product;
        }

        throw new ForbiddenException("Você não tem permissão para acessar este produto");
    }

    @Transactional
    public ProductModel update(Integer id, UpdateProductDTO dto) {
        ProductModel product = findById(id);

        if (dto.name() != null && !dto.name().isBlank()) product.setName(dto.name());
        if (dto.description() != null && !dto.description().isBlank()) product.setDescription(dto.description());
        if (dto.price() != null) product.setPrice(dto.price());

        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities() != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().startsWith("ROLE_ADMIN"));

        if (isAdmin && dto.maxXp() != null) {
            product.setMaxXp(dto.maxXp());
        } else if (dto.price() != null || dto.categoryId() != null) {
            CategoryModel category = dto.categoryId() != null
                ? categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"))
                : product.getCategory();
            product.setMaxXp(xpCalculationStrategy.calculate(product.getPrice(), category.getCategoryWeight()));
        }

        if (dto.stock() != null) {
            product.setStock(dto.stock());
        }

        if (dto.shopkeeperId() != null) {
            ShopkeeperModel shopkeeper = shopkeeperRepository.findById(dto.shopkeeperId())
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found"));
            product.setShopkeeper(shopkeeper);
        }

        if (dto.categoryId() != null) {
            CategoryModel category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        if (dto.poiId() != null) {
            PoiModel poi = poiRepository.findById(dto.poiId())
                .orElseThrow(() -> new ResourceNotFoundException("POI not found"));
            product.setPoi(poi);
        }

        if (dto.active() != null) product.setActive(dto.active());
        if (dto.highlight() != null) product.setHighlight(dto.highlight());

        return repository.save(product);
    }

    @Transactional
    public void delete(Integer id) {
        ProductModel product = findById(id);
        repository.delete(product);
    }

    public List<ProductModel> findAll() {
        List<ProductModel> products = repository.findAll();
        products.forEach(this::enrichImageUrls);
        return products;
    }

    public List<ProductModel> getRecentProducts() {
        List<ProductModel> products = repository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 50)).getContent();
        products.forEach(this::enrichImageUrls);
        return products;
    }

    public List<ProductModel> findByShopkeeper(Integer shopkeeperId) {
        List<ProductModel> products = repository.findByShopkeeperId(shopkeeperId);
        products.forEach(this::enrichImageUrls);
        return products;
    }

    public List<ProductModel> findByShopkeeperWithStock(Integer shopkeeperId) {
        List<ProductModel> products = repository.findByShopkeeperIdAndStockGreaterThan(shopkeeperId, 0);
        products.forEach(this::enrichImageUrls);
        return products;
    }

    public CatalogMetricsDTO getCatalogMetrics(Integer shopkeeperId) {
        return new CatalogMetricsDTO(
            repository.countByShopkeeperId(shopkeeperId),
            repository.countByShopkeeperIdAndActiveTrue(shopkeeperId),
            repository.countByShopkeeperIdAndHighlightTrue(shopkeeperId)
        );
    }

    @Transactional
    public ProductModel addImage(Integer id, MultipartFile image) {
        ProductModel product = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getImages().size() >= 4) {
            throw new InvalidRequestException("Maximum of 4 images per product");
        }

        String imageName = storageService.uploadFile(image, "products");
        product.getImages().add(imageName);
        ProductModel saved = repository.save(product);
        enrichImageUrls(saved);
        return saved;
    }

    @Transactional
    public ProductModel removeImage(Integer id, int index) {
        ProductModel product = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        List<String> images = product.getImages();
        if (index < 0 || index >= images.size()) {
            throw new InvalidRequestException("Invalid image index");
        }

        String removedImage = images.remove(index);
        storageService.deleteFile(removedImage);
        ProductModel saved = repository.save(product);
        enrichImageUrls(saved);
        return saved;
    }

    private void enrichImageUrls(ProductModel product) {
        List<String> urls = new ArrayList<>();
        for (String imageName : product.getImages()) {
            urls.add(storageService.getFileUrl(imageName));
        }
        product.setImageUrls(urls);
    }
}
