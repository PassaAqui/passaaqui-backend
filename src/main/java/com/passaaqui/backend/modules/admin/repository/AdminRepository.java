package com.passaaqui.backend.modules.admin.repository;

import com.passaaqui.backend.modules.admin.model.AdminModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<AdminModel, Integer> {

    boolean existsByEmail(String email);

    Optional<AdminModel> findById(Integer id);

    Optional<AdminModel> findByEmail(String email);

}