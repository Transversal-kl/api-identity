package com.fv.billpay.api.identity.service;

import com.fv.billpay.api.identity.dto.request.UserRequestDto;
import com.fv.billpay.api.identity.dto.request.UserUpdateDto;
import com.fv.billpay.api.identity.dto.response.UserGroupResponseDto;
import com.fv.billpay.api.identity.dto.response.UserResponseDto;
import com.fv.billpay.api.identity.dto.response.UserRoleResponseDto;
import io.smallrye.mutiny.Uni;

import java.util.List;

/**
 * Interfaz del servicio reactivo para gestión de usuarios.
 */
public interface IUserService {

    /**
     * Crea un usuario en Keycloak y PostgreSQL.
     */
    Uni<UserResponseDto> createUser(UserRequestDto userRequestDto);

    /**
     * Actualiza un usuario en Keycloak y PostgreSQL.
     */
    Uni<UserResponseDto> updateUser(String userId, UserUpdateDto userUpdateDto);

    /**
     * Elimina un usuario de Keycloak y PostgreSQL.
     */
    Uni<Void> deleteUser(String userId);

    /**
     * Obtiene un usuario por su ID.
     */
    Uni<UserResponseDto> getUserById(String userId);

    /**
     * Obtiene todos los usuarios con paginación.
     */
    Uni<List<UserResponseDto>> getAllUsers(int page, int size);

    /**
     * Busca usuarios por username.
     */
    Uni<List<UserResponseDto>> searchUsersByUsername(String username);

    /**
     * Actualiza la imagen de perfil de un usuario.
     */
    Uni<Void> updateProfileImage(String userId, byte[] profileImage);

    /**
     * Obtiene la imagen de perfil de un usuario.
     */
    Uni<byte[]> getProfileImage(String userId);

    /**
     * Asigna grupos a un usuario.
     */
    Uni<List<UserGroupResponseDto>> assignGroupsToUser(String userId, List<String> groupIds);

    /**
     * Remueve grupos de un usuario.
     */
    Uni<List<UserGroupResponseDto>> removeGroupsFromUser(String userId, List<String> groupIds);

    /**
     * Obtiene los grupos de un usuario.
     */
    Uni<List<UserGroupResponseDto>> getUserGroups(String userId);

    /**
     * Asigna roles a un usuario.
     */
    Uni<List<UserRoleResponseDto>> assignRolesToUser(String userId, List<String> roleNames);

    /**
     * Remueve roles de un usuario.
     */
    Uni<List<UserRoleResponseDto>> removeRolesFromUser(String userId, List<String> roleNames);

    /**
     * Obtiene los roles de un usuario.
     */
    Uni<List<UserRoleResponseDto>> getUserRoles(String userId);
}
