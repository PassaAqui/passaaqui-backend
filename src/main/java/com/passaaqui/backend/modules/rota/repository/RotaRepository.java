package com.passaaqui.backend.modules.rota.repository;

import com.passaaqui.backend.modules.rota.model.RotaModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RotaRepository extends JpaRepository<RotaModel, UUID> {
}
