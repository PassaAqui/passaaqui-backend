package com.passaaqui.backend.modules.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.passaaqui.backend.modules.auth.model.AuthModel;

public interface AuthRepository extends JpaRepository<AuthModel, Integer> {
    Optional<AuthModel> findByDeviceId(String deviceId);
}
