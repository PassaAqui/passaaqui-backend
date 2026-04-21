package com.passaaqui.backend.modules.city.repository;

import com.passaaqui.backend.modules.city.model.CityModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<CityModel, Integer> {
}
