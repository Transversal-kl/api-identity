package com.fv.billpay.api.role.service;

import com.fv.billpay.api.role.dto.request.UserRequestDto;
import com.fv.billpay.api.role.dto.request.UserUpdateDto;
import com.fv.billpay.api.role.dto.response.UserResponseDto;
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
}
