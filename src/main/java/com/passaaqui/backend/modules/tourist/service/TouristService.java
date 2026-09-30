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
    private final com.passaaqui.backend.modules.poi.repository.PoiVisitRepository poiVisitRepository;
    private final com.passaaqui.backend.infra.integration.storage.StorageService storageService;

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
        List<TouristModel> tourists = repository.findAll();
        for (TouristModel tourist : tourists) {
            if (tourist.getImage() != null) {
                tourist.setImageUrl(storageService.getFileUrl(tourist.getImage()));
            }
        }
        return tourists;
    }

    public TouristModel findById(Integer id) {
        TouristModel tourist = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));
        if (tourist.getImage() != null) {
            tourist.setImageUrl(storageService.getFileUrl(tourist.getImage()));
        }
        return tourist;
    }

    public TouristModel findByIdOrEmail(String identifier) {
        TouristModel tourist;
        if (identifier.contains("@")) {
            tourist = repository.findByEmail(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));
        } else {
            try {
                tourist = repository.findById(Integer.parseInt(identifier))
                    .orElseThrow(() -> new ResourceNotFoundException("Tourist not found"));
            } catch (NumberFormatException e) {
                throw new InvalidRequestException("Invalid identifier format");
            }
        }
        if (tourist.getImage() != null) {
            tourist.setImageUrl(storageService.getFileUrl(tourist.getImage()));
        }
        return tourist;
    }

    @Transactional
    public TouristModel update(String identifier, UpdateTouristDTO dto) {
        TouristModel tourist = findByIdOrEmail(identifier);
        if (dto.name() != null && !dto.name().isBlank()) tourist.setName(dto.name());
        if (dto.password() != null && !dto.password().isBlank()) tourist.setPassword(passwordEncoder.encode(dto.password()));
        if (dto.documentId() != null && !dto.documentId().isBlank()) tourist.setDocumentId(dto.documentId());
        if (dto.theme() != null) tourist.setTheme(dto.theme());
        return repository.save(tourist);
    }

    @Transactional
    public void delete(String identifier) {
        TouristModel tourist = findByIdOrEmail(identifier);
        repository.delete(tourist);
    }

    public List<com.passaaqui.backend.modules.poi.dto.PoiTravelHistoryDTO> getPoiTravelHistory(Integer touristId) {
        if (!repository.existsById(touristId)) {
            throw new ResourceNotFoundException("Tourist not found");
        }

        return poiVisitRepository.findByUserIdOrderByVisitedAtDesc(touristId).stream()
                .map(visit -> {
                    var poi = visit.getPoi();
                    String imageUrl = null;
                    if (poi != null && poi.getImage() != null && !poi.getImage().isBlank()) {
                        imageUrl = storageService.getFileUrl(poi.getImage());
                    }

                    return new com.passaaqui.backend.modules.poi.dto.PoiTravelHistoryDTO(
                            visit.getId(),
                            poi != null ? poi.getId() : null,
                            poi != null ? poi.getName() : null,
                            poi != null ? poi.getDescription() : null,
                            imageUrl,
                            poi != null ? poi.getType() : null,
                            poi != null && poi.getCity() != null ? poi.getCity().getName() : null,
                            visit.getXpEarned(),
                            visit.getDistanceKm(),
                            visit.getVisitedAt()
                    );
                })
                .toList();
    }

}
