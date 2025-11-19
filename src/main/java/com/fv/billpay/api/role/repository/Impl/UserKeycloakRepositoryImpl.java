package com.fv.billpay.api.role.repository.Impl;

import com.fv.billpay.api.role.dto.request.UserRequestDto;
import com.fv.billpay.api.role.dto.request.UserUpdateDto;
import com.fv.billpay.api.role.exception.KeycloakSyncException;
import com.fv.billpay.api.role.exception.UserAlreadyExistsException;
import com.fv.billpay.api.role.exception.UserNotFoundException;
import com.fv.billpay.api.role.repository.IUserKeycloakRepository;
import com.fv.billpay.api.role.utils.KeycloakAdminProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
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
                throw new UserAlreadyExistsException(userRequestDto.getUsername());
            } else {
                String errorMessage = response.readEntity(String.class);
                response.close();
                log.error("Error al crear usuario en Keycloak. Status: {}, Error: {}", 
                    response.getStatus(), errorMessage);
                throw new KeycloakSyncException("Error al crear usuario: " + errorMessage);
            }
        } catch (UserAlreadyExistsException | KeycloakSyncException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al crear usuario en Keycloak", e);
            throw new KeycloakSyncException("Error inesperado al crear usuario", e);
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
            throw new UserNotFoundException(userId);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al actualizar usuario en Keycloak: {}", userId, e);
            throw new KeycloakSyncException("Error al actualizar usuario", e);
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
            throw new UserNotFoundException(userId);
        } catch (Exception e) {
            log.error("Error al eliminar usuario de Keycloak: {}", userId, e);
            throw new KeycloakSyncException("Error al eliminar usuario", e);
        }
    }

    @Override
    public UserRepresentation getUserById(String userId) {
        try {
            UsersResource usersResource = getUsersResource();

            return usersResource.get(userId).toRepresentation();
        } catch (NotFoundException e) {
            log.error("Usuario no encontrado en Keycloak: {}", userId);
            throw new UserNotFoundException(userId);
        } catch (Exception e) {
            log.error("Error al obtener usuario de Keycloak: {}", userId, e);
            throw new KeycloakSyncException("Error al obtener usuario", e);
        }
    }

    @Override
    public List<UserRepresentation> getAllUsers(int first, int max) {
        try {
            UsersResource usersResource = getUsersResource();

            return usersResource.list(first, max);
        } catch (Exception e) {
            log.error("Error al obtener usuarios de Keycloak", e);
            throw new KeycloakSyncException("Error al obtener usuarios", e);
        }
    }

    @Override
    public List<UserRepresentation> searchUsersByUsername(String username) {
        try {
            UsersResource usersResource = getUsersResource();

            return usersResource.search(username, 0, 100);
        } catch (Exception e) {
            log.error("Error al buscar usuarios en Keycloak", e);
            throw new KeycloakSyncException("Error al buscar usuarios", e);
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
            throw new KeycloakSyncException("Error al verificar usuario", e);
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
            throw new KeycloakSyncException("Error al establecer contraseña", e);
        }
    }

    @Override
    public void assignGroupToUser(String userId, String groupId) {
        try {
            UsersResource usersResource = getUsersResource();
            UserResource userResource = usersResource.get(userId);
            
            userResource.joinGroup(groupId);
            
            log.info("Grupo '{}' asignado exitosamente al usuario: {}", groupId, userId);
        } catch (NotFoundException e) {
            log.error("Usuario o grupo no encontrado: userId={}, groupId={}", userId, groupId);
            throw new UserNotFoundException("Usuario o grupo no encontrado");
        } catch (Exception e) {
            log.error("Error al asignar grupo al usuario: userId={}, groupId={}", userId, groupId, e);
            throw new KeycloakSyncException("Error al asignar grupo al usuario", e);
        }
    }

    @Override
    public void removeGroupFromUser(String userId, String groupId) {
        try {
            UsersResource usersResource = getUsersResource();
            UserResource userResource = usersResource.get(userId);
            
            userResource.leaveGroup(groupId);
            
            log.info("Grupo '{}' removido exitosamente del usuario: {}", groupId, userId);
        } catch (NotFoundException e) {
            log.error("Usuario o grupo no encontrado: userId={}, groupId={}", userId, groupId);
            throw new UserNotFoundException("Usuario o grupo no encontrado");
        } catch (Exception e) {
            log.error("Error al remover grupo del usuario: userId={}, groupId={}", userId, groupId, e);
            throw new KeycloakSyncException("Error al remover grupo del usuario", e);
        }
    }

    @Override
    public List<GroupRepresentation> getUserGroups(String userId) {
        try {
            UsersResource usersResource = getUsersResource();
            UserResource userResource = usersResource.get(userId);
            
            List<GroupRepresentation> groups = userResource.groups();
            log.debug("Obtenidos {} grupos para usuario: {}", groups.size(), userId);
            return groups;
        } catch (NotFoundException e) {
            log.error("Usuario no encontrado: {}", userId);
            throw new UserNotFoundException(userId);
        } catch (Exception e) {
            log.error("Error al obtener grupos del usuario: {}", userId, e);
            throw new KeycloakSyncException("Error al obtener grupos del usuario", e);
        }
    }

    @Override
    public void assignRoleToUser(String userId, String roleName) {
        try {
            UsersResource usersResource = getUsersResource();
            UserResource userResource = usersResource.get(userId);
            RolesResource rolesResource = keycloakAdminProvider.getRolesResource();
            
            // Obtener el rol
            RoleRepresentation role = rolesResource.get(roleName).toRepresentation();
            
            // Asignar rol realm al usuario
            userResource.roles().realmLevel().add(Collections.singletonList(role));
            
            log.info("Rol '{}' asignado exitosamente al usuario: {}", roleName, userId);
        } catch (NotFoundException e) {
            log.error("Usuario o rol no encontrado: userId={}, roleName={}", userId, roleName);
            throw new UserNotFoundException("Usuario o rol no encontrado");
        } catch (Exception e) {
            log.error("Error al asignar rol al usuario: userId={}, roleName={}", userId, roleName, e);
            throw new KeycloakSyncException("Error al asignar rol al usuario", e);
        }
    }

    @Override
    public void removeRoleFromUser(String userId, String roleName) {
        try {
            UsersResource usersResource = getUsersResource();
            UserResource userResource = usersResource.get(userId);
            RolesResource rolesResource = keycloakAdminProvider.getRolesResource();
            
            // Obtener el rol
            RoleRepresentation role = rolesResource.get(roleName).toRepresentation();
            
            // Remover rol realm del usuario
            userResource.roles().realmLevel().remove(Collections.singletonList(role));
            
            log.info("Rol '{}' removido exitosamente del usuario: {}", roleName, userId);
        } catch (NotFoundException e) {
            log.error("Usuario o rol no encontrado: userId={}, roleName={}", userId, roleName);
            throw new UserNotFoundException("Usuario o rol no encontrado");
        } catch (Exception e) {
            log.error("Error al remover rol del usuario: userId={}, roleName={}", userId, roleName, e);
            throw new KeycloakSyncException("Error al remover rol del usuario", e);
        }
    }

    @Override
    public List<RoleRepresentation> getUserRoles(String userId) {
        try {
            UsersResource usersResource = getUsersResource();
            UserResource userResource = usersResource.get(userId);
            
            // Obtener roles realm asignados al usuario
            List<RoleRepresentation> roles = userResource.roles().realmLevel().listAll();
            log.debug("Obtenidos {} roles para usuario: {}", roles.size(), userId);
            return roles;
        } catch (NotFoundException e) {
            log.error("Usuario no encontrado: {}", userId);
            throw new UserNotFoundException(userId);
        } catch (Exception e) {
            log.error("Error al obtener roles del usuario: {}", userId, e);
            throw new KeycloakSyncException("Error al obtener roles del usuario", e);
        }
    }
}
