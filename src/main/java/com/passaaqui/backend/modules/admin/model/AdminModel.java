package com.passaaqui.backend.modules.admin.model;

import com.passaaqui.backend.modules.admin.model.enums.AdminType;
import com.passaaqui.backend.modules.user.model.UserModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class AdminModel extends UserModel {
    @Column(nullable = false)
    private AdminType adminType;

}
