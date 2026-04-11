package br.com.recifego.api.modules.shopkeeper.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.recifego.api.modules.shopkeeper.model.ShopkeeperModel;

public interface ShopkeeperRepository extends JpaRepository<ShopkeeperModel, Integer> {

    Optional<ShopkeeperModel> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
}
