package com.passaaqui.backend.modules.poi.repository;

import com.passaaqui.backend.modules.poi.model.PoiModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PoiRepository extends JpaRepository<PoiModel, Integer> {
}
