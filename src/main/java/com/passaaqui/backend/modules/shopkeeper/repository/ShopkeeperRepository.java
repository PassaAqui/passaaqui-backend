package com.passaaqui.backend.modules.shopkeeper.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;

public interface ShopkeeperRepository extends JpaRepository<ShopkeeperModel, Integer> {

    Optional<ShopkeeperModel> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
}
