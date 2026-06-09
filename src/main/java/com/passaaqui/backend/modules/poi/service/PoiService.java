package com.passaaqui.backend.modules.poi.service;

import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.city.model.CityModel;
import com.passaaqui.backend.modules.city.repository.CityRepository;
import com.passaaqui.backend.modules.poi.dto.CreatePoiDTO;
import com.passaaqui.backend.modules.poi.dto.UpdatePoiDTO;
import com.passaaqui.backend.modules.poi.model.PoiModel;
import com.passaaqui.backend.modules.poi.repository.PoiRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class PoiService {

    private final PoiRepository repository;
    private final CityRepository cityRepository;
    private final StorageService storageService;

    @Transactional
    public PoiModel create(CreatePoiDTO dto, MultipartFile image) {
        CityModel city = cityRepository.findById(dto.cityId())
            .orElseThrow(() -> new ResourceNotFoundException("City not found"));

        PoiModel poi = new PoiModel();
        poi.setName(dto.name());
        poi.setDescription(dto.description());
        poi.setXpReward(dto.xpReward());
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

    public PoiModel findById(Integer id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("POI not found"));
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
}
