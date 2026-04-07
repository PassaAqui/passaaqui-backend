package br.com.recifego.api.modules.tourist.service;

import org.springframework.stereotype.Service;

import br.com.recifego.api.infra.exception.ConflictException;
import br.com.recifego.api.modules.tourist.model.TouristModel;
import br.com.recifego.api.modules.tourist.repository.TouristRepository;
import br.com.recifego.api.modules.user.model.enums.UserRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TouristService {
    
    private final TouristRepository repository;

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

}
