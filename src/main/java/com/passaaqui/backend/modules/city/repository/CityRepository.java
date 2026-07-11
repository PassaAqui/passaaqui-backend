package com.passaaqui.backend.modules.city.repository;

import com.passaaqui.backend.modules.city.model.CityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CityRepository extends JpaRepository<CityModel, Integer> {

    @Query("SELECT c FROM CityModel c WHERE c.minLatitude IS NOT NULL AND c.maxLatitude IS NOT NULL AND c.minLongitude IS NOT NULL AND c.maxLongitude IS NOT NULL AND :latitude BETWEEN c.minLatitude AND c.maxLatitude AND :longitude BETWEEN c.minLongitude AND c.maxLongitude")
    Optional<CityModel> findByCoordinatesWithinBoundingBox(@Param("latitude") Double latitude, @Param("longitude") Double longitude);
}
