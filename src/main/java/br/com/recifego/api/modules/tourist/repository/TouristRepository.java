package br.com.recifego.api.modules.tourist.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.recifego.api.modules.tourist.model.TouristModel;

public interface TouristRepository extends JpaRepository<TouristModel, Integer> {

    Optional<TouristModel> findByEmail(String email);

    boolean existsByEmail(String email);

}
