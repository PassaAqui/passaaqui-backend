package br.com.recifego.api.modules.tourist.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.recifego.api.infra.exception.ConflictException;
import br.com.recifego.api.infra.exception.InvalidRequestException;
import br.com.recifego.api.infra.exception.ResourceNotFoundException;
import br.com.recifego.api.modules.tourist.dto.UpdateTouristDTO;
import br.com.recifego.api.modules.tourist.model.TouristModel;
import br.com.recifego.api.modules.tourist.repository.TouristRepository;
import br.com.recifego.api.modules.user.model.enums.UserRole;
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
