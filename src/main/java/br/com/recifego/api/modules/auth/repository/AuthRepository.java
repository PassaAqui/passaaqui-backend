package br.com.recifego.api.modules.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.recifego.api.modules.auth.model.AuthModel;

public interface AuthRepository extends JpaRepository<AuthModel, Integer> {
    Optional<AuthModel> findByDeviceId(String deviceId);
}
