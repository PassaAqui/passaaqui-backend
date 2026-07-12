package com.passaaqui.backend.modules.city.controller;

import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.city.dto.CreateCityDTO;
import com.passaaqui.backend.modules.city.dto.LocateCityDTO;
import com.passaaqui.backend.modules.city.dto.UpdateCityDTO;
import com.passaaqui.backend.modules.city.model.CityModel;
import com.passaaqui.backend.modules.city.service.CityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/city")
@PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;
    private final StorageService storageService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CityModel> createCity(
            @RequestPart("data") @Valid CreateCityDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        CityModel city = cityService.createCity(dto.ibgeCode(), dto.description(), dto.minLatitude(), dto.maxLatitude(), dto.minLongitude(), dto.maxLongitude(), image);
        if (city.getImage() != null) {
            city.setImageUrl(storageService.getFileUrl(city.getImage()));
        }
        return ResponseEntity.ok(city);
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CityModel> createCityJson(@RequestBody @Valid CreateCityDTO dto) {
        CityModel city = cityService.createCity(dto.ibgeCode(), dto.description(), dto.minLatitude(), dto.maxLatitude(), dto.minLongitude(), dto.maxLongitude(), null);
        if (city.getImage() != null) {
            city.setImageUrl(storageService.getFileUrl(city.getImage()));
        }
        return ResponseEntity.ok(city);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CityModel> updateCity(@PathVariable Integer id, @RequestBody @Valid UpdateCityDTO dto) {
        CityModel city = cityService.updateCity(id, dto);
        if (city.getImage() != null) {
            city.setImageUrl(storageService.getFileUrl(city.getImage()));
        }
        return ResponseEntity.ok(city);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Integer id) {
        cityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CityModel> getById(@PathVariable Integer id) {
        CityModel city = cityService.getCityById(id);
        if (city.getImage() != null) {
            city.setImageUrl(storageService.getFileUrl(city.getImage()));
        }
        return ResponseEntity.ok(city);
    }

    @GetMapping()
    public ResponseEntity<List<CityModel>> getAll() {
        List<CityModel> cities = cityService.getAllCities();
        for (CityModel city : cities) {
            if (city.getImage() != null) {
                city.setImageUrl(storageService.getFileUrl(city.getImage()));
            }
        }
        return ResponseEntity.ok(cities);
    }

    @PostMapping("/locate")
    @PreAuthorize("hasAnyRole('TOURIST', 'SHOPKEEPER', 'ADMIN_USER', 'ADMIN_ROOT')")
    public ResponseEntity<CityModel> locateCity(@RequestBody @Valid LocateCityDTO dto) {
        CityModel city = cityService.locateCity(dto.latitude(), dto.longitude());
        if (city.getImage() != null) {
            city.setImageUrl(storageService.getFileUrl(city.getImage()));
        }
        return ResponseEntity.ok(city);
    }
}
