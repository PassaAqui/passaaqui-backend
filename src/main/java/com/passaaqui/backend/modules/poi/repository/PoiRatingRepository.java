package com.passaaqui.backend.modules.poi.repository;

import com.passaaqui.backend.modules.poi.model.PoiRatingModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PoiRatingRepository extends JpaRepository<PoiRatingModel, Integer> {

    List<PoiRatingModel> findByPoiId(Integer poiId);

    Optional<PoiRatingModel> findByPoiIdAndUserId(Integer poiId, Integer userId);

    long countByPoiId(Integer poiId);
}
