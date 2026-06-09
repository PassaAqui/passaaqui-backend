package com.passaaqui.backend.modules.user.service;

import java.util.List;

import com.passaaqui.backend.infra.integration.storage.StorageService;
import com.passaaqui.backend.modules.user.dto.UpdateUserDTO;
import com.passaaqui.backend.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.user.model.UserModel;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StorageService storageService;

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

    @Transactional
    public UserModel update(Integer id, UpdateUserDTO dto, MultipartFile image) {
        UserModel user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (dto.name() != null && !dto.name().isBlank()) {
            user.setName(dto.name());
        }

        if (image != null && !image.isEmpty()) {
            String imageName = storageService.uploadFile(image, "users");
            user.setImage(imageName);
        }

        return userRepository.save(user);
    }
}
