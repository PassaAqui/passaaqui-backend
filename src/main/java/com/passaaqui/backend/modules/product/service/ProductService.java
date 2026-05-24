package com.passaaqui.backend.modules.product.service;

import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.category.model.CategoryModel;
import com.passaaqui.backend.modules.category.repository.CategoryRepository;
import com.passaaqui.backend.modules.product.dto.CreateProductDTO;
import com.passaaqui.backend.modules.product.dto.UpdateProductDTO;
import com.passaaqui.backend.modules.product.model.ProductModel;
import com.passaaqui.backend.modules.product.repository.ProductRepository;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import com.passaaqui.backend.modules.shopkeeper.repository.ShopkeeperRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final ShopkeeperRepository shopkeeperRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductModel create(CreateProductDTO dto) {
        ShopkeeperModel shopkeeper = shopkeeperRepository.findById(dto.shopkeeperId())
            .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found"));

        CategoryModel category = categoryRepository.findById(dto.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        ProductModel product = new ProductModel();
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setXpCost(dto.xpCost());
        product.setShopkeeper(shopkeeper);
        product.setCategory(category);

        return repository.save(product);
    }

    public ProductModel findById(Integer id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Transactional
    public ProductModel update(Integer id, UpdateProductDTO dto) {
        ProductModel product = findById(id);

        if (dto.name() != null && !dto.name().isBlank()) product.setName(dto.name());
        if (dto.description() != null && !dto.description().isBlank()) product.setDescription(dto.description());
        if (dto.price() != null) product.setPrice(dto.price());
        if (dto.xpCost() != null) product.setXpCost(dto.xpCost());

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

        return repository.save(product);
    }

    @Transactional
    public void delete(Integer id) {
        ProductModel product = findById(id);
        repository.delete(product);
    }

    public List<ProductModel> findAll() {
        return repository.findAll();
    }

    public List<ProductModel> getRecentProducts() {
        return repository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 50)).getContent();
    }
}
