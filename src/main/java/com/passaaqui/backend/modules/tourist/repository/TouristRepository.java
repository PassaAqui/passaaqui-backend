package com.passaaqui.backend.modules.tourist.repository;

import java.util.Optional;

import com.passaaqui.backend.modules.tourist.model.TouristModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TouristRepository extends JpaRepository<TouristModel, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TouristModel t WHERE t.id = :id")
    Optional<TouristModel> findByIdForUpdate(@Param("id") Integer id);

    Optional<TouristModel> findByEmail(String email);

    boolean existsByEmail(String email);
}
