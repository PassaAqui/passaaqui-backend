package com.passaaqui.backend.modules.shopkeeper.model;

import com.passaaqui.backend.modules.category.model.CategoryModel;
import com.passaaqui.backend.modules.user.model.UserModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ShopkeeperModel extends UserModel {
    @Column(nullable = false)
    private String documentId;

    @Column(nullable = false)
    private String companyName;

    private String description;

    @ManyToOne
    @JoinColumn(nullable = false)
    private CategoryModel category;
}
