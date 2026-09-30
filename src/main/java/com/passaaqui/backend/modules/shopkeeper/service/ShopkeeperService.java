package com.passaaqui.backend.modules.shopkeeper.service;

import java.util.List;

import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.category.model.CategoryModel;
import com.passaaqui.backend.modules.category.repository.CategoryRepository;
import com.passaaqui.backend.modules.city.model.CityModel;
import com.passaaqui.backend.modules.city.repository.CityRepository;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.model.enums.PoiType;
import com.passaaqui.backend.modules.poi.repository.PoiRepository;
import com.passaaqui.backend.modules.shopkeeper.repository.ShopkeeperRepository;
import com.passaaqui.backend.modules.user.model.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.passaaqui.backend.infra.exception.ConflictException;
import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.shopkeeper.dto.ShopkeeperProfileDTO;
import com.passaaqui.backend.modules.shopkeeper.dto.UpdateShopkeeperDTO;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShopkeeperService {

    private final ShopkeeperRepository repository;
    private final CategoryRepository categoryRepository;
    private final PoiRepository poiRepository;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;

    @Transactional
    public ShopkeeperModel createUser(String email, String name, String password, String documentId, String companyName, String description, Integer categoryId,
                                      String poiName, String poiDescription, Double latitude, Double longitude,
                                      Double minLatitude, Double maxLatitude, Double minLongitude, Double maxLongitude, Integer cityId,
                                      MultipartFile image) {
        if (repository.existsByEmail(email))
            throw new ConflictException("There is already a user with this account.");

        CategoryModel category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        ShopkeeperModel newShopkeeper = new ShopkeeperModel();
        newShopkeeper.setEmail(email);
        newShopkeeper.setName(name);
        newShopkeeper.setPassword(password);
        newShopkeeper.setDocumentId(documentId);
        newShopkeeper.setCompanyName(companyName);
        newShopkeeper.setDescription(description);
        newShopkeeper.setCategory(category);
        newShopkeeper.setRole(UserRole.SHOPKEEPER);

        repository.save(newShopkeeper);

        CityModel city = cityRepository.findById(cityId)
            .orElseThrow(() -> new ResourceNotFoundException("City not found"));

        PoiModel poi = new PoiModel();
        poi.setName(poiName);
        poi.setDescription(poiDescription);
        poi.setType(PoiType.STORE);
        poi.setXpReward(null);
        poi.setLatitude(latitude);
        poi.setLongitude(longitude);
        poi.setMinLatitude(minLatitude);
        poi.setMaxLatitude(maxLatitude);
        poi.setMinLongitude(minLongitude);
        poi.setMaxLongitude(maxLongitude);
        poi.setCity(city);

        if (image != null && !image.isEmpty()) {
            String imageName = storageService.uploadFile(image, "pois");
            poi.setImage(imageName);
        }

        poi.setShopkeeper(newShopkeeper);
        poiRepository.save(poi);

        return newShopkeeper;
    }

    private void populateImageUrl(ShopkeeperModel shopkeeper) {
        if (shopkeeper.getImage() != null && !shopkeeper.getImage().isBlank()) {
            shopkeeper.setImageUrl(storageService.getFileUrl(shopkeeper.getImage()));
        }
    }

    public List<ShopkeeperModel> findAll() {
        List<ShopkeeperModel> shopkeepers = repository.findAll();
        shopkeepers.forEach(this::populateImageUrl);
        return shopkeepers;
    }

    public ShopkeeperModel findById(Integer id) {
        ShopkeeperModel shopkeeper = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found"));
        populateImageUrl(shopkeeper);
        return shopkeeper;
    }

    public ShopkeeperProfileDTO findProfileById(Integer id) {
        ShopkeeperModel shopkeeper = findById(id);
        PoiModel poi = poiRepository.findByShopkeeperId(id)
                .orElseThrow(() -> new ResourceNotFoundException("POI not found for this shopkeeper"));
        if (poi.getImage() != null && !poi.getImage().isBlank()) {
            poi.setImageUrl(storageService.getFileUrl(poi.getImage()));
        }
        return ShopkeeperProfileDTO.from(shopkeeper, poi);
    }

    public ShopkeeperModel findByIdOrEmail(String identifier) {
        ShopkeeperModel shopkeeper;
        if (identifier.contains("@")) {
            shopkeeper = repository.findByEmail(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found"));
        } else {
            try {
                shopkeeper = repository.findById(Integer.parseInt(identifier))
                    .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found"));
            } catch (NumberFormatException e) {
                throw new InvalidRequestException("Invalid identifier format");
            }
        }
        populateImageUrl(shopkeeper);
        return shopkeeper;
    }

    @Transactional
    public ShopkeeperModel update(String identifier, UpdateShopkeeperDTO dto) {
        ShopkeeperModel shopkeeper = findByIdOrEmail(identifier);
        applyUpdateDto(shopkeeper, dto);
        ShopkeeperModel saved = repository.save(shopkeeper);
        populateImageUrl(saved);
        return saved;
    }

    @Transactional
    public ShopkeeperProfileDTO updateProfile(Integer shopkeeperId, UpdateShopkeeperDTO dto, MultipartFile image) {
        ShopkeeperModel shopkeeper = findById(shopkeeperId);
        applyUpdateDto(shopkeeper, dto);

        if (image != null && !image.isEmpty()) {
            String imageName = storageService.uploadFile(image, "users");
            shopkeeper.setImage(imageName);
        }

        ShopkeeperModel saved = repository.save(shopkeeper);
        populateImageUrl(saved);

        PoiModel poi = poiRepository.findByShopkeeperId(shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("POI not found for this shopkeeper"));
        if (poi.getImage() != null && !poi.getImage().isBlank()) {
            poi.setImageUrl(storageService.getFileUrl(poi.getImage()));
        }

        return ShopkeeperProfileDTO.from(saved, poi);
    }

    @Transactional
    public ShopkeeperProfileDTO updateAvatar(Integer shopkeeperId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new InvalidRequestException("Image file must not be empty");
        }

        ShopkeeperModel shopkeeper = findById(shopkeeperId);
        String imageName = storageService.uploadFile(image, "users");
        shopkeeper.setImage(imageName);

        ShopkeeperModel saved = repository.save(shopkeeper);
        populateImageUrl(saved);

        PoiModel poi = poiRepository.findByShopkeeperId(shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("POI not found for this shopkeeper"));
        if (poi.getImage() != null && !poi.getImage().isBlank()) {
            poi.setImageUrl(storageService.getFileUrl(poi.getImage()));
        }

        return ShopkeeperProfileDTO.from(saved, poi);
    }

    private void applyUpdateDto(ShopkeeperModel shopkeeper, UpdateShopkeeperDTO dto) {
        if (dto == null) return;
        if (dto.name() != null && !dto.name().isBlank()) shopkeeper.setName(dto.name());
        if (dto.password() != null && !dto.password().isBlank()) shopkeeper.setPassword(passwordEncoder.encode(dto.password()));
        if (dto.documentId() != null && !dto.documentId().isBlank()) shopkeeper.setDocumentId(dto.documentId());
        if (dto.companyName() != null && !dto.companyName().isBlank()) shopkeeper.setCompanyName(dto.companyName());
        if (dto.description() != null && !dto.description().isBlank()) shopkeeper.setDescription(dto.description());
        if (dto.categoryId() != null) {
            CategoryModel category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            shopkeeper.setCategory(category);
        }
        if (dto.theme() != null) {
            shopkeeper.setTheme(dto.theme());
        }
    }

    @Transactional
    public void delete(String identifier) {
        ShopkeeperModel shopkeeper = findByIdOrEmail(identifier);
        repository.delete(shopkeeper);
    }

}
