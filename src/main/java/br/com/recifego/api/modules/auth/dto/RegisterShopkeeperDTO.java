package br.com.recifego.api.modules.auth.dto;

import br.com.recifego.api.shared.validation.document.Document;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterShopkeeperDTO(
    @NotNull
    @Email
    String email,

    @NotNull
    String name,

    @NotNull
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters")
    String password,

    @NotNull
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters")
    String confirm_password,

    @NotNull
    @Document(message = "Invalid document")
    @Size(min = 14, max = 18, message = "Invalid document")
    String documentId,

    @NotNull
    String companyName
) {
    
}
