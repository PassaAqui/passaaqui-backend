package com.passaaqui.backend.modules.user.service;

import java.util.List;

import com.passaaqui.backend.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.user.model.UserModel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;

    public List<UserModel> findAll() {
        return userRepository.findAll();
    }

    public UserModel findByIdOrEmail(String identifier) {
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        try {
            return userRepository.findById(Integer.parseInt(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Invalid identifier format");
        }
    }

    public UserModel findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

}
