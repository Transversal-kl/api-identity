package com.fv.billpay.api.role.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * DTO de respuesta para información de usuarios.
 * Incluye URL para la imagen de perfil en lugar del contenido binario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private String id;
    
    private String username;
    
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private Boolean enabled;
    
    private ZonedDateTime createdAt;
    
    private Boolean hasProfileImage;
}
