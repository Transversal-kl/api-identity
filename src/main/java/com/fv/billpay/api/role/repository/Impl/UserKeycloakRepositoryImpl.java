package com.fv.billpay.api.role.repository.Impl;

import com.fv.billpay.api.role.dto.request.UserRequestDto;
import com.fv.billpay.api.role.dto.request.UserUpdateDto;
import com.fv.billpay.api.role.repository.IUserKeycloakRepository;
import com.fv.billpay.api.role.utils.KeycloakAdminProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Collections;
import java.util.List;

/**
 * Implementación del repositorio para operaciones con usuarios en Keycloak.
 */
@ApplicationScoped
@Slf4j
public class UserKeycloakRepositoryImpl implements IUserKeycloakRepository {

    @Inject
    KeycloakAdminProvider keycloakAdminProvider;

    /**
     * Helper method para obtener UsersResource de Keycloak.
     */
    private UsersResource getUsersResource() {
        return keycloakAdminProvider.getKeycloak()
            .realm(keycloakAdminProvider.getRealm())
            .users();
    }

    @Override
    public String createUser(UserRequestDto userRequestDto) {
        try {
            UsersResource usersResource = getUsersResource();

            // Crear representación del usuario
            UserRepresentation user = new UserRepresentation();
            user.setUsername(userRequestDto.getUsername());
            user.setEmail(userRequestDto.getEmail());
            user.setFirstName(userRequestDto.getFirstName());
            user.setLastName(userRequestDto.getLastName());
            user.setEnabled(userRequestDto.getEnabled() != null ? userRequestDto.getEnabled() : true);
            user.setEmailVerified(false);

            // Crear usuario
            Response response = usersResource.create(user);
            
            if (response.getStatus() == 201) {
                // Extraer ID del usuario creado
                String locationHeader = response.getHeaderString("Location");
                String userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
                
                // Establecer contraseña
                setUserPassword(userId, userRequestDto.getPassword());
                
                log.info("Usuario creado exitosamente en Keycloak: {} (ID: {})", 
                    userRequestDto.getUsername(), userId);
                
                response.close();
                return userId;
            } else if (response.getStatus() == 409) {
                response.close();
                throw new WebApplicationException(
                    "El usuario con username '" + userRequestDto.getUsername() + "' ya existe",
                    Response.Status.CONFLICT
                );
            } else {
                String errorMessage = response.readEntity(String.class);
                response.close();
                log.error("Error al crear usuario en Keycloak. Status: {}, Error: {}", 
                    response.getStatus(), errorMessage);
                throw new WebApplicationException(
                    "Error al crear usuario en Keycloak: " + errorMessage,
                    Response.Status.INTERNAL_SERVER_ERROR
                );
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al crear usuario en Keycloak", e);
            throw new WebApplicationException(
                "Error inesperado al crear usuario: " + e.getMessage(),
                Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public void updateUser(String userId, UserUpdateDto userUpdateDto) {
        try {
            UsersResource usersResource = getUsersResource();

            // Obtener usuario actual
            UserRepresentation user = usersResource.get(userId).toRepresentation();

            // Actualizar solo los campos proporcionados
            // NOTA: username NO se puede actualizar en Keycloak una vez creado
            if (userUpdateDto.getEmail() != null) {
                user.setEmail(userUpdateDto.getEmail());
            }
            if (userUpdateDto.getFirstName() != null) {
                user.setFirstName(userUpdateDto.getFirstName());
            }
            if (userUpdateDto.getLastName() != null) {
                user.setLastName(userUpdateDto.getLastName());
            }
            if (userUpdateDto.getEnabled() != null) {
                user.setEnabled(userUpdateDto.getEnabled());
            }

            // Actualizar usuario
            usersResource.get(userId).update(user);
            
            log.info("Usuario actualizado exitosamente en Keycloak: {}", userId);
            
            // Advertir si se intentó cambiar username
            if (userUpdateDto.getUsername() != null && 
                !userUpdateDto.getUsername().equals(user.getUsername())) {
                log.warn("Se intentó cambiar username de {} a {} pero Keycloak no permite cambiar username", 
                    user.getUsername(), userUpdateDto.getUsername());
            }
        } catch (NotFoundException e) {
            log.error("Usuario no encontrado en Keycloak: {}", userId);
            throw new WebApplicationException(
                "Usuario con ID '" + userId + "' no encontrado",
                Response.Status.NOT_FOUND
            );
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar usuario en Keycloak: {}", userId, e);
            throw new WebApplicationException(
                "Error al actualizar usuario en Keycloak: " + e.getMessage(),
                Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public void deleteUser(String userId) {
        try {
            UsersResource usersResource = getUsersResource();

            usersResource.get(userId).remove();
            
            log.info("Usuario eliminado exitosamente de Keycloak: {}", userId);
        } catch (NotFoundException e) {
            log.error("Usuario no encontrado en Keycloak: {}", userId);
            throw new WebApplicationException(
                "Usuario con ID '" + userId + "' no encontrado",
                Response.Status.NOT_FOUND
            );
        } catch (Exception e) {
            log.error("Error al eliminar usuario de Keycloak: {}", userId, e);
            throw new WebApplicationException(
                "Error al eliminar usuario: " + e.getMessage(),
                Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public UserRepresentation getUserById(String userId) {
        try {
            UsersResource usersResource = getUsersResource();

            return usersResource.get(userId).toRepresentation();
        } catch (NotFoundException e) {
            log.error("Usuario no encontrado en Keycloak: {}", userId);
            throw new WebApplicationException(
                "Usuario con ID '" + userId + "' no encontrado",
                Response.Status.NOT_FOUND
            );
        } catch (Exception e) {
            log.error("Error al obtener usuario de Keycloak: {}", userId, e);
            throw new WebApplicationException(
                "Error al obtener usuario: " + e.getMessage(),
                Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public List<UserRepresentation> getAllUsers(int first, int max) {
        try {
            UsersResource usersResource = getUsersResource();

            return usersResource.list(first, max);
        } catch (Exception e) {
            log.error("Error al obtener usuarios de Keycloak", e);
            throw new WebApplicationException(
                "Error al obtener usuarios: " + e.getMessage(),
                Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public List<UserRepresentation> searchUsersByUsername(String username) {
        try {
            UsersResource usersResource = getUsersResource();

            return usersResource.search(username, 0, 100);
        } catch (Exception e) {
            log.error("Error al buscar usuarios en Keycloak", e);
            throw new WebApplicationException(
                "Error al buscar usuarios: " + e.getMessage(),
                Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public boolean userExists(String userId) {
        try {
            UsersResource usersResource = getUsersResource();

            usersResource.get(userId).toRepresentation();
            return true;
        } catch (NotFoundException e) {
            return false;
        } catch (Exception e) {
            log.error("Error al verificar existencia de usuario en Keycloak: {}", userId, e);
            throw new WebApplicationException(
                "Error al verificar usuario: " + e.getMessage(),
                Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Establece la contraseña de un usuario.
     */
    private void setUserPassword(String userId, String password) {
        try {
            UsersResource usersResource = getUsersResource();

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);

            usersResource.get(userId).resetPassword(credential);
            
            log.info("Contraseña establecida para usuario: {}", userId);
        } catch (Exception e) {
            log.error("Error al establecer contraseña para usuario: {}", userId, e);
            throw new WebApplicationException(
                "Error al establecer contraseña: " + e.getMessage(),
                Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }
}
