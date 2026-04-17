package com.passaaqui.backend.modules.tourist.repository;

import java.util.Optional;

import com.passaaqui.backend.modules.tourist.model.TouristModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TouristRepository extends JpaRepository<TouristModel, Integer> {

    Optional<TouristModel> findByEmail(String email);

    boolean existsByEmail(String email);

}
