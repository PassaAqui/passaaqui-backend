package com.passaaqui.backend.modules.city.service;

import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.ibge.IbgeClient;
import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.city.model.CityModel;
import com.passaaqui.backend.modules.city.repository.CityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final IbgeClient ibgeClient;
    private final StorageService storageService;

    @Transactional
    public CityModel createCity(String ibgeCode, String description, Double minLatitude, Double maxLatitude, Double minLongitude, Double maxLongitude, MultipartFile image) {
        var response = ibgeClient.getCityByIbgeCode(ibgeCode);

        CityModel city = new CityModel();

        city.setIbgeCode(response.getId().toString());
        city.setName(response.getNome());

        city.setState(response.getMicrorregiao()
                .getMesorregiao()
                .getUF()
                .getSigla());

        city.setStateName(response.getMicrorregiao()
                .getMesorregiao()
                .getUF()
                .getNome());

        city.setRegion(response.getMicrorregiao()
                .getMesorregiao()
                .getUF()
                .getRegiao()
                .getNome());

        city.setRegionCode(response.getMicrorregiao()
                .getMesorregiao()
                .getUF()
                .getRegiao()
                .getId());

        city.setMicroRegion(response.getMicrorregiao().getNome());

        city.setMesoRegion(response.getMicrorregiao()
                .getMesorregiao()
                .getNome());

        city.setDescription(description);
        city.setMinLatitude(minLatitude);
        city.setMaxLatitude(maxLatitude);
        city.setMinLongitude(minLongitude);
        city.setMaxLongitude(maxLongitude);

        if (image != null && !image.isEmpty()) {
            String imageName = storageService.uploadFile(image, "cities");
            city.setImage(imageName);
        }

        cityRepository.save(city);

        return city;
    }

    @Transactional
    public CityModel updateCity(Integer id, com.passaaqui.backend.modules.city.dto.UpdateCityDTO dto) {
        CityModel city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));

        city.setName(dto.name());
        city.setDescription(dto.description());
        city.setState(dto.state());
        city.setIbgeCode(dto.ibgeCode());
        city.setRegion(dto.region());
        city.setMicroRegion(dto.microRegion());
        city.setMesoRegion(dto.mesoRegion());
        city.setStateName(dto.stateName());
        city.setRegionCode(dto.regionCode());
        city.setMinLatitude(dto.minLatitude());
        city.setMaxLatitude(dto.maxLatitude());
        city.setMinLongitude(dto.minLongitude());
        city.setMaxLongitude(dto.maxLongitude());

        return cityRepository.save(city);
    }

    @Transactional
    public void deleteCity(Integer id) {
        CityModel city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));

        cityRepository.delete(city);
    }

    public CityModel getCityById(Integer id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));
    }

    public List<CityModel> getAllCities() {
        return cityRepository.findAll();
    }

    public CityModel locateCity(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new InvalidRequestException("Latitude and longitude are required");
        }
        if (latitude < -90 || latitude > 90) {
            throw new InvalidRequestException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new InvalidRequestException("Longitude must be between -180 and 180");
        }

        return cityRepository.findByCoordinatesWithinBoundingBox(latitude, longitude)
                .orElseThrow(() -> new ResourceNotFoundException("No city found for the given coordinates"));
    }
}
