package com.passaaqui.backend.modules.city.service;

import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.infra.integration.ibge.IbgeClient;
import com.passaaqui.backend.modules.city.model.CityModel;
import com.passaaqui.backend.modules.city.repository.CityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final IbgeClient ibgeClient;

    @Transactional
    public Mono<CityModel> createCity(String ibgeCode, String description) {
        return ibgeClient.getCityByIbgeCode(ibgeCode).map(response -> {
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

            cityRepository.save(city);

            return city;
        });
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

        return cityRepository.save(city);
    }

    @Transactional
    public void deleteCity(Integer id) {
        CityModel city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));

        cityRepository.delete(city);
    }

    public List<CityModel> getAllCities() {
        return cityRepository.findAll();
    }

}
