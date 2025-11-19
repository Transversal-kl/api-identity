package com.fv.billpay.api.identity.mapper;

import com.fv.billpay.api.identity.dto.response.UserResponseDto;
import com.fv.billpay.api.identity.entity.UserAccount;
import org.keycloak.representations.idm.UserRepresentation;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Mapper para convertir entre entidades de usuario y DTOs.
 */
public class UserMapper {

    private UserMapper() {
        // Utility class
    }

    /**
     * Convierte un UserAccount a UserResponseDto.
     */
    public static UserResponseDto toResponseDto(UserAccount userAccount, UserRepresentation keycloakUser) {
        return UserResponseDto.builder()
                .id(userAccount.getId().toString())
                .username(userAccount.getUsername())
                .email(userAccount.getEmail())
                .firstName(userAccount.getFirstName())
                .lastName(userAccount.getLastName())
                .enabled(keycloakUser != null ? keycloakUser.isEnabled() : null)
                .createdAt(userAccount.getCreatedAt())
                .hasProfileImage(userAccount.getProfileImage() != null && userAccount.getProfileImage().length > 0)
                .build();
    }

    /**
     * Convierte un UserRepresentation de Keycloak a UserAccount.
     */
    public static UserAccount fromKeycloakUser(UserRepresentation keycloakUser) {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(UUID.fromString(keycloakUser.getId()));
        userAccount.setUsername(keycloakUser.getUsername());
        userAccount.setEmail(keycloakUser.getEmail());
        userAccount.setFirstName(keycloakUser.getFirstName());
        userAccount.setLastName(keycloakUser.getLastName());
        
        // Convertir timestamp de Keycloak a ZonedDateTime
        if (keycloakUser.getCreatedTimestamp() != null) {
            userAccount.setCreatedAt(
                ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(keycloakUser.getCreatedTimestamp()),
                    ZoneId.systemDefault()
                )
            );
        } else {
            userAccount.setCreatedAt(ZonedDateTime.now());
        }
        
        return userAccount;
    }
}
