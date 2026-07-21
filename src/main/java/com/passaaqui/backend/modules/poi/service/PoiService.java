package com.passaaqui.backend.modules.poi.service;

import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.city.model.CityModel;
import com.passaaqui.backend.modules.city.repository.CityRepository;
import com.passaaqui.backend.modules.poi.dto.CreatePoiDTO;
import com.passaaqui.backend.modules.poi.dto.PoiDetailDTO;
import com.passaaqui.backend.modules.poi.dto.PoiNearbyDTO;
import com.passaaqui.backend.modules.poi.dto.UpdatePoiDTO;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.model.enums.PoiType;
import com.passaaqui.backend.modules.poi.repository.PoiRepository;
import com.passaaqui.backend.modules.product.dto.ProductDTO;
import com.passaaqui.backend.modules.product.repository.ProductRepository;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PoiService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double DEFAULT_RADIUS_KM = 1.0;

    private static final Map<String, Double> RADIUS_BY_MODE = Map.of(
        "foot-walking", 1.0,
        "foot-hiking", 1.5,
        "cycling-regular", 5.0,
        "cycling-road", 7.0,
        "cycling-mountain", 5.0,
        "cycling-electric", 8.0,
        "driving-car", 20.0,
        "driving-hgv", 15.0,
        "wheelchair", 0.5
    );

    private final PoiRepository repository;
    private final CityRepository cityRepository;
    private final StorageService storageService;
    private final ProductRepository productRepository;

    @Transactional
    public PoiModel create(CreatePoiDTO dto, MultipartFile image) {
        CityModel city = cityRepository.findById(dto.cityId())
            .orElseThrow(() -> new ResourceNotFoundException("City not found"));

        PoiModel poi = new PoiModel();
        poi.setName(dto.name());
        poi.setDescription(dto.description());
        poi.setType(dto.type());
        poi.setLatitude(dto.latitude());
        poi.setLongitude(dto.longitude());
        poi.setMinLatitude(dto.minLatitude());
        poi.setMaxLatitude(dto.maxLatitude());
        poi.setMinLongitude(dto.minLongitude());
        poi.setMaxLongitude(dto.maxLongitude());
        poi.setCity(city);

        if (image != null && !image.isEmpty()) {
            String imageName = storageService.uploadFile(image, "pois");
            poi.setImage(imageName);
        }

        return repository.save(poi);
    }

    public Page<PoiModel> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<PoiNearbyDTO> findNearby(Double latitude, Double longitude, String mode) {
        double radius = getRadiusForMode(mode);

        double latDelta = Math.toDegrees(radius / EARTH_RADIUS_KM);
        double lonDelta = Math.toDegrees(radius / (EARTH_RADIUS_KM * Math.cos(Math.toRadians(latitude))));

        List<PoiModel> candidates = repository.findByBoundingBox(
            latitude - latDelta, latitude + latDelta,
            longitude - lonDelta, longitude + lonDelta
        );

        return candidates.stream()
            .map(poi -> {
                double distance = GeoUtils.haversineKm(latitude, longitude, poi.getLatitude(), poi.getLongitude());
                String imageUrl = poi.getImage() != null ? storageService.getFileUrl(poi.getImage()) : null;
                return PoiNearbyDTO.from(poi, distance, imageUrl);
            })
            .filter(dto -> dto.distanceKm() <= radius)
            .sorted(Comparator.comparingDouble(PoiNearbyDTO::distanceKm))
            .toList();
    }

    public PoiModel findById(Integer id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("POI not found"));
    }

    public PoiDetailDTO findDetailById(Integer id) {
        PoiModel poi = findById(id);
        String imageUrl = poi.getImage() != null ? storageService.getFileUrl(poi.getImage()) : null;

        List<ProductDTO> products = Collections.emptyList();
        if (poi.getType() == PoiType.STORE) {
            products = productRepository.findByPoiId(id).stream()
                .map(p -> {
                    List<String> urls = p.getImages().stream()
                        .map(storageService::getFileUrl)
                        .toList();
                    return ProductDTO.from(p, urls);
                })
                .toList();
        }

        return PoiDetailDTO.from(poi, imageUrl, products);
    }

    @Transactional
    public PoiModel update(Integer id, UpdatePoiDTO dto) {
        PoiModel poi = findById(id);

        if (dto.name() != null && !dto.name().isBlank()) poi.setName(dto.name());
        if (dto.description() != null && !dto.description().isBlank()) poi.setDescription(dto.description());
        if (dto.xpReward() != null) poi.setXpReward(dto.xpReward());
        if (dto.latitude() != null) poi.setLatitude(dto.latitude());
        if (dto.longitude() != null) poi.setLongitude(dto.longitude());
        if (dto.minLatitude() != null) poi.setMinLatitude(dto.minLatitude());
        if (dto.maxLatitude() != null) poi.setMaxLatitude(dto.maxLatitude());
        if (dto.minLongitude() != null) poi.setMinLongitude(dto.minLongitude());
        if (dto.maxLongitude() != null) poi.setMaxLongitude(dto.maxLongitude());

        if (dto.cityId() != null) {
            CityModel city = cityRepository.findById(dto.cityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));
            poi.setCity(city);
        }

        return repository.save(poi);
    }

    @Transactional
    public void delete(Integer id) {
        PoiModel poi = findById(id);
        repository.delete(poi);
    }

    @Transactional
    public PoiModel setXpReward(Integer id, Integer xpReward) {
        PoiModel poi = findById(id);
        poi.setXpReward(xpReward);
        return repository.save(poi);
    }

    @Transactional
    public PoiModel updateImage(Integer id, MultipartFile image) {
        PoiModel poi = findById(id);
        if (image != null && !image.isEmpty()) {
            String imageName = storageService.uploadFile(image, "pois");
            poi.setImage(imageName);
        }
        return repository.save(poi);
    }

    private double getRadiusForMode(String mode) {
        if (mode == null) return DEFAULT_RADIUS_KM;
        return RADIUS_BY_MODE.getOrDefault(mode, DEFAULT_RADIUS_KM);
    }
}
