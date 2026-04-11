package br.com.recifego.api.modules.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.recifego.api.infra.exception.InvalidRequestException;
import br.com.recifego.api.infra.exception.ResourceNotFoundException;
import br.com.recifego.api.modules.user.model.UserModel;
import br.com.recifego.api.modules.user.repository.UserRepository;
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
