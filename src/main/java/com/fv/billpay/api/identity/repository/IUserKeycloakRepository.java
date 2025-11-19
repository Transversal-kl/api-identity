package com.fv.billpay.api.identity.repository;

import com.fv.billpay.api.identity.dto.request.UserRequestDto;
import com.fv.billpay.api.identity.dto.request.UserUpdateDto;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
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

    /**
     * Asigna un grupo a un usuario en Keycloak.
     */
    void assignGroupToUser(String userId, String groupId);

    /**
     * Remueve un grupo de un usuario en Keycloak.
     */
    void removeGroupFromUser(String userId, String groupId);

    /**
     * Obtiene los grupos asignados a un usuario en Keycloak.
     */
    List<GroupRepresentation> getUserGroups(String userId);

    /**
     * Asigna un rol realm a un usuario en Keycloak.
     */
    void assignRoleToUser(String userId, String roleName);

    /**
     * Remueve un rol realm de un usuario en Keycloak.
     */
    void removeRoleFromUser(String userId, String roleName);

    /**
     * Obtiene los roles realm asignados a un usuario en Keycloak.
     */
    List<RoleRepresentation> getUserRoles(String userId);
}
