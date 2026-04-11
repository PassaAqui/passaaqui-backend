package br.com.recifego.api.modules.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.recifego.api.modules.user.model.UserModel;
import br.com.recifego.api.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserController {

    private UserService service;

    @GetMapping
    public ResponseEntity<List<UserModel>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<UserModel> findByIdentifier(@PathVariable String identifier) {
        return ResponseEntity.ok(service.findByIdOrEmail(identifier));
    }
    
}
