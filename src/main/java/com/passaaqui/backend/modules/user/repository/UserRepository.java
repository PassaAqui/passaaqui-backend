package com.passaaqui.backend.modules.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.passaaqui.backend.modules.user.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, Integer> {

    Optional<UserModel> findByEmail(String email);
    
}
