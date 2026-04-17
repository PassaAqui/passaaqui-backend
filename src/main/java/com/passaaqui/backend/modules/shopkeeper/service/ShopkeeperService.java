package com.passaaqui.backend.modules.shopkeeper.service;

import java.util.List;

import com.passaaqui.backend.modules.shopkeeper.repository.ShopkeeperRepository;
import com.passaaqui.backend.modules.user.model.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.passaaqui.backend.infra.exception.ConflictException;
import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.shopkeeper.dto.UpdateShopkeeperDTO;
import com.passaaqui.backend.modules.shopkeeper.model.ShopkeeperModel;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShopkeeperService {
    
    private final ShopkeeperRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ShopkeeperModel createUser(String email, String name, String password, String documentId, String companyName) {
        if (repository.existsByEmail(email))
            throw new ConflictException("There is already a user with this account.");

        ShopkeeperModel newShopkeeper = new ShopkeeperModel();
        newShopkeeper.setEmail(email);
        newShopkeeper.setName(name);
        newShopkeeper.setPassword(password);
        newShopkeeper.setDocumentId(documentId);
        newShopkeeper.setCompanyName(companyName);
        newShopkeeper.setRole(UserRole.SHOPKEEPER);

        repository.save(newShopkeeper);

        return newShopkeeper;
    }

    public List<ShopkeeperModel> findAll() {
        return repository.findAll();
    }

    public ShopkeeperModel findByIdOrEmail(String identifier) {
        if (identifier.contains("@")) {
            return repository.findByEmail(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found"));
        }
        try {
            return repository.findById(Integer.parseInt(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found"));
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Invalid identifier format");
        }
    }

    @Transactional
    public ShopkeeperModel update(String identifier, UpdateShopkeeperDTO dto) {
        ShopkeeperModel shopkeeper = findByIdOrEmail(identifier);
        if (dto.name() != null && !dto.name().isBlank()) shopkeeper.setName(dto.name());
        if (dto.password() != null && !dto.password().isBlank()) shopkeeper.setPassword(passwordEncoder.encode(dto.password()));
        if (dto.documentId() != null && !dto.documentId().isBlank()) shopkeeper.setDocumentId(dto.documentId());
        if (dto.companyName() != null && !dto.companyName().isBlank()) shopkeeper.setCompanyName(dto.companyName());
        return repository.save(shopkeeper);
    }

    @Transactional
    public void delete(String identifier) {
        ShopkeeperModel shopkeeper = findByIdOrEmail(identifier);
        repository.delete(shopkeeper);
    }

}
