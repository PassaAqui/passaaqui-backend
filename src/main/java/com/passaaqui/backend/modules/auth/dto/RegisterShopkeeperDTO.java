package com.passaaqui.backend.modules.auth.dto;

import com.passaaqui.backend.shared.validation.document.Document;
import com.passaaqui.backend.shared.validation.password.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterShopkeeperDTO(
    @NotNull
    @Email
    String email,

    @NotNull
    String name,

    @NotNull
    @Password( message = "Password must be between 8 and 16 characters long and include at least one letter, one number, and one special character." )
    String password,

    @NotNull
    @Password( message = "Password must be between 8 and 16 characters long and include at least one letter, one number, and one special character." )
    String confirm_password,

    @NotNull
    @Document(message = "Invalid document")
    @Size(min = 11, max = 18, message = "Invalid document")
    String documentId,

    @NotNull
    String companyName,

    String description,

    @NotNull
    Integer categoryId,

    @NotBlank
    String poiName,

    String poiDescription,

    Double latitude,

    Double longitude,

    Double minLatitude,

    Double maxLatitude,

    Double minLongitude,

    Double maxLongitude,

    @NotNull
    Integer cityId
) {
    
}
