package com.passaaqui.backend.shared.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JWTObject {

    private String refresh_token, access_token;
    
}
