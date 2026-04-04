package br.com.recifego.api.modules.shopkeeper.model;

import br.com.recifego.api.modules.user.model.UserModel;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ShopkeeperModel extends UserModel {

    private String documentId; // cpf ou cnpj
    private String companyName;

}
