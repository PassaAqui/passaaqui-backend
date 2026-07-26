package com.passaaqui.backend.modules.poi.repository;

import com.passaaqui.backend.modules.poi.model.PoiModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PoiRepository extends JpaRepository<PoiModel, Integer> {
    Optional<PoiModel> findByShopkeeperId(Integer shopkeeperId);

    @Query("SELECT p FROM PoiModel p WHERE p.latitude IS NOT NULL AND p.longitude IS NOT NULL " +
           "AND p.latitude BETWEEN :minLat AND :maxLat AND p.longitude BETWEEN :minLon AND :maxLon")
    List<PoiModel> findByBoundingBox(
        @Param("minLat") Double minLat,
        @Param("maxLat") Double maxLat,
        @Param("minLon") Double minLon,
        @Param("maxLon") Double maxLon
    );
}
