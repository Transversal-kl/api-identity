package com.fv.billpay.api.role.repository;

import com.fv.billpay.api.role.dto.request.UserRequestDto;
import com.fv.billpay.api.role.dto.request.UserUpdateDto;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

/**
 * Interfaz del repositorio para operaciones con usuarios en Keycloak.
 */
public interface IUserKeycloakRepository {

    /**
     * Crea un usuario en Keycloak y retorna su ID.
     */
    String createUser(UserRequestDto userRequestDto);

    /**
     * Actualiza un usuario en Keycloak.
     */
    void updateUser(String userId, UserUpdateDto userUpdateDto);

    /**
     * Elimina un usuario de Keycloak.
     */
    void deleteUser(String userId);

    /**
     * Obtiene un usuario de Keycloak por su ID.
     */
    UserRepresentation getUserById(String userId);

    /**
     * Obtiene todos los usuarios de Keycloak con paginación.
     */
    List<UserRepresentation> getAllUsers(int first, int max);

    /**
     * Busca usuarios por username.
     */
    List<UserRepresentation> searchUsersByUsername(String username);

    /**
     * Verifica si un usuario existe en Keycloak.
     */
    boolean userExists(String userId);
}
