package br.com.recifego.api.modules.user.service;

import org.springframework.stereotype.Service;

import br.com.recifego.api.modules.user.model.UserModel;
import br.com.recifego.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;

    public UserModel findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

}
