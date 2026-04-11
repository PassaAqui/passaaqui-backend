package br.com.recifego.api.modules.auth.dto;

import br.com.recifego.api.shared.validation.document.Document;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterTouristDTO(
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
    @Size(min = 11, max = 14, message = "Invalid document")
    String documentId

) {
    
}
