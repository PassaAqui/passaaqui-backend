package br.com.recifego.api.modules.shopkeeper.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.recifego.api.modules.shopkeeper.dto.UpdateShopkeeperDTO;
import br.com.recifego.api.modules.shopkeeper.model.ShopkeeperModel;
import br.com.recifego.api.modules.shopkeeper.service.ShopkeeperService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shopkeepers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ShopkeeperController {

    private final ShopkeeperService service;

    @GetMapping
    public ResponseEntity<List<ShopkeeperModel>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<ShopkeeperModel> findByIdentifier(@PathVariable String identifier) {
        return ResponseEntity.ok(service.findByIdOrEmail(identifier));
    }

    @PutMapping("/{identifier}")
    public ResponseEntity<ShopkeeperModel> update(@PathVariable String identifier, @RequestBody UpdateShopkeeperDTO dto) {
        return ResponseEntity.ok(service.update(identifier, dto));
    }

    @DeleteMapping("/{identifier}")
    public ResponseEntity<Void> delete(@PathVariable String identifier) {
        service.delete(identifier);
        return ResponseEntity.noContent().build();
    }
}