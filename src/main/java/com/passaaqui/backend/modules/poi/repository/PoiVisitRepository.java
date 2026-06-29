package com.passaaqui.backend.modules.poi.repository;

import com.passaaqui.backend.modules.poi.model.PoiVisitModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PoiVisitRepository extends JpaRepository<PoiVisitModel, Integer> {

    long countByPoiIdAndVisitedAtAfterAndUserIdNot(Integer poiId, LocalDateTime since, Integer userId);

    Optional<PoiVisitModel> findFirstByPoiIdAndUserIdOrderByVisitedAtDesc(Integer poiId, Integer userId);
}
