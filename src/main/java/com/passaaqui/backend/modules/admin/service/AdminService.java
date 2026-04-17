package com.passaaqui.backend.modules.admin.service;

import com.passaaqui.backend.infra.exception.ConflictException;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import com.passaaqui.backend.modules.admin.model.enums.AdminType;
import com.passaaqui.backend.modules.admin.model.AdminModel;
import com.passaaqui.backend.modules.admin.repository.AdminRepository;
import com.passaaqui.backend.modules.user.model.enums.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AdminService {

    private AdminRepository adminRepository;

    public AdminModel createAdmin(String email, String name, String password) {
        if (adminRepository.existsByEmail(email))
            throw new ConflictException("The administrator email is already in use.");

        AdminModel newAdmin = new AdminModel();
        newAdmin.setRole(UserRole.ADMIN);
        newAdmin.setAdminType(AdminType.USER);
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

    public AdminModel updateAdmin(Integer id, String email, String name, String password, AdminType adminType, UserRole role) {
        AdminModel admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));

        admin.setEmail(email);
        admin.setName(name);
        admin.setPassword(password);
        admin.setAdminType(adminType);
        admin.setRole(role);

        return adminRepository.save(admin);
    }

    public void deleteAdmin(Integer id) {
        AdminModel admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));

        adminRepository.delete(admin);
    }
}
