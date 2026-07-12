package com.passaaqui.backend.modules.tourist.service;

import java.util.List;

import com.passaaqui.backend.modules.user.model.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.passaaqui.backend.infra.exception.ConflictException;
import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.tourist.dto.UpdateTouristDTO;
import com.passaaqui.backend.modules.tourist.model.TouristModel;
import com.passaaqui.backend.modules.tourist.repository.TouristRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TouristService {
    
    private final TouristRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TouristModel createUser(String email, String name, String password, String documentId) {
        TouristModel newTourist = new TouristModel();

        if (repository.existsByEmail(email)) 
            throw new ConflictException("There is already a user with this account.");

        newTourist.setName(name);
        newTourist.setEmail(email);
        newTourist.setPassword(password);
        newTourist.setLevel(0);
        newTourist.setCurrentXP(0);
        newTourist.setDocumentId(documentId);
        newTourist.setRole(UserRole.TOURIST);

        repository.save(newTourist);

        return newTourist;
    }

    public List<TouristModel> findAll() {
        return repository.findAll();
    }

    public TouristModel findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));
    }

    public TouristModel findByIdOrEmail(String identifier) {
        if (identifier.contains("@")) {
            return repository.findByEmail(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));
        }
        try {
            return repository.findById(Integer.parseInt(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Invalid identifier format");
        }
    }

    @Transactional
    public TouristModel update(String identifier, UpdateTouristDTO dto) {
        TouristModel tourist = findByIdOrEmail(identifier);
        if (dto.name() != null && !dto.name().isBlank()) tourist.setName(dto.name());
        if (dto.password() != null && !dto.password().isBlank()) tourist.setPassword(passwordEncoder.encode(dto.password()));
        if (dto.documentId() != null && !dto.documentId().isBlank()) tourist.setDocumentId(dto.documentId());
        return repository.save(tourist);
    }

    @Transactional
    public void delete(String identifier) {
        TouristModel tourist = findByIdOrEmail(identifier);
        repository.delete(tourist);
    }

}
