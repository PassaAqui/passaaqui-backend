package com.passaaqui.backend.modules.admin.service;

import com.passaaqui.backend.infra.exception.ConflictException;
import com.passaaqui.backend.infra.exception.ForbiddenException;
import com.passaaqui.backend.infra.exception.InvalidRequestException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.admin.model.enums.AdminType;
import com.passaaqui.backend.modules.admin.model.AdminModel;
import com.passaaqui.backend.modules.admin.repository.AdminRepository;
import com.passaaqui.backend.modules.user.model.enums.UserRole;
import com.passaaqui.backend.modules.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AdminService {

    private AdminRepository adminRepository;
    private UserRepository userRepository;

    @Transactional
    public AdminModel createAdmin(String email, String name, String password, AdminType adminType) {
        if (userRepository.findByEmail(email).isPresent())
            throw new ConflictException("The administrator email is already in use.");

        AdminModel newAdmin = new AdminModel();
        newAdmin.setRole(UserRole.ADMIN);
        newAdmin.setAdminType(adminType);
        newAdmin.setEmail(email);
        newAdmin.setName(name);
        newAdmin.setPassword(password);

        adminRepository.save(newAdmin);

        return newAdmin;
    }

    public List<AdminModel> getAll() {
        return adminRepository.findAll();
    }

    public AdminModel getOneById(Integer id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
    }

    public AdminModel getOneByEmail(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + email));
    }

    private void ensureNotOtherRoot(Integer targetId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = (String) auth.getPrincipal();

        if (currentUserId.equals(targetId.toString())) return;

        AdminModel target = adminRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + targetId));

        if (target.getAdminType() == AdminType.ROOT) {
            throw new ForbiddenException("ROOT administrators cannot edit or delete another ROOT administrator.");
        }
    }

    @Transactional
    public AdminModel updateAdmin(Integer id, String email, String name, String password, AdminType adminType) {
        ensureNotOtherRoot(id);

        AdminModel admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));

        admin.setEmail(email);
        admin.setName(name);
        admin.setAdminType(adminType);

        if (password != null && !password.isBlank()) {
            if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,16}$")) {
                throw new InvalidRequestException("Password must be 8-16 characters with at least one letter, one number, and one special character.");
            }
            admin.setPassword(password);
        }

        return adminRepository.save(admin);
    }

    @Transactional
    public void deleteAdmin(Integer id) {
        ensureNotOtherRoot(id);

        AdminModel admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));

        adminRepository.delete(admin);
    }
}
