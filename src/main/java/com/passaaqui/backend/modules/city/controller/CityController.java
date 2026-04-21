package com.passaaqui.backend.modules.city.controller;

import com.passaaqui.backend.modules.city.dto.CreateCityDTO;
import com.passaaqui.backend.modules.city.dto.UpdateCityDTO;
import com.passaaqui.backend.modules.city.model.CityModel;
import com.passaaqui.backend.modules.city.service.CityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/city")
@PreAuthorize("hasAnyRole('ADMIN_USER', 'ADMIN_ROOT')")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @PostMapping("/create")
    public Mono<ResponseEntity<CityModel>> createCity(@RequestBody @Valid CreateCityDTO dto) {
        return cityService.createCity(dto.ibgeCode(), dto.description()).map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CityModel> updateCity(@PathVariable Integer id, @RequestBody @Valid UpdateCityDTO dto) {
        return ResponseEntity.ok(cityService.updateCity(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Integer id) {
        cityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    public ResponseEntity<List<CityModel>> getAll() {
        return ResponseEntity.ok(cityService.getAllCities());
    }
}