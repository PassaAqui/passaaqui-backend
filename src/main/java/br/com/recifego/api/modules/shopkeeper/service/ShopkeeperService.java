package br.com.recifego.api.modules.shopkeeper.service;

import org.springframework.stereotype.Service;

import br.com.recifego.api.infra.exception.ConflictException;
import br.com.recifego.api.modules.shopkeeper.model.ShopkeeperModel;
import br.com.recifego.api.modules.shopkeeper.repository.ShopkeeperRepository;
import br.com.recifego.api.modules.user.model.enums.UserRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShopkeeperService {
    
    private final ShopkeeperRepository repository;

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

}
