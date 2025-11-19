package com.fv.billpay.api.role.service.Impl;

import com.fv.billpay.api.role.dto.request.UserRequestDto;
import com.fv.billpay.api.role.dto.request.UserUpdateDto;
import com.fv.billpay.api.role.dto.response.UserGroupResponseDto;
import com.fv.billpay.api.role.dto.response.UserResponseDto;
import com.fv.billpay.api.role.dto.response.UserRoleResponseDto;
import com.fv.billpay.api.role.entity.UserAccount;
import com.fv.billpay.api.role.exception.InvalidUserDataException;
import com.fv.billpay.api.role.exception.KeycloakSyncException;
import com.fv.billpay.api.role.exception.UserNotFoundException;
import com.fv.billpay.api.role.mapper.UserMapper;
import com.fv.billpay.api.role.repository.IUserKeycloakRepository;
import com.fv.billpay.api.role.repository.UserAccountRepository;
import com.fv.billpay.api.role.service.IUserService;
import com.fv.billpay.api.role.utils.UuidValidator;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación reactiva del servicio para gestión de usuarios.
 * Sincroniza usuarios entre Keycloak y PostgreSQL usando Mutiny.
 */
@ApplicationScoped
@Slf4j
public class UserServiceImpl implements IUserService {

    @Inject
    IUserKeycloakRepository userKeycloakRepository;

    @Inject
    UserAccountRepository userAccountRepository;

    @Override
    @WithTransaction
    public Uni<UserResponseDto> createUser(UserRequestDto userRequestDto) {
        return Uni.createFrom().item(() -> {
            // 1. Crear usuario en Keycloak (bloqueante)
            String userId = userKeycloakRepository.createUser(userRequestDto);
            
            // 2. Obtener el usuario creado de Keycloak
            UserRepresentation keycloakUser = userKeycloakRepository.getUserById(userId);
            
            return UserMapper.fromKeycloakUser(keycloakUser);
        })
        .chain(userAccount -> {
            // 3. Sincronizar en PostgreSQL (reactivo)
            return userAccountRepository.persist(userAccount)
                .invoke(() -> log.info("Usuario creado y sincronizado exitosamente: {} (ID: {})", 
                    userRequestDto.getUsername(), userAccount.getId()))
                .map(persisted -> {
                    UserRepresentation keycloakUser = userKeycloakRepository.getUserById(
                        persisted.getId().toString()
                    );
                    return UserMapper.toResponseDto(persisted, keycloakUser);
                });
        })
        .onFailure().transform(error -> {
            // Las excepciones personalizadas ya vienen del repository
            if (error instanceof UserNotFoundException ||
                error instanceof InvalidUserDataException ||
                error instanceof KeycloakSyncException) {
                return error;
            }
            log.error("Error inesperado al crear usuario", error);
            return new KeycloakSyncException("Error inesperado al crear usuario", error);
        });
    }

    @Override
    @WithTransaction
    public Uni<UserResponseDto> updateUser(String userId, UserUpdateDto userUpdateDto) {
        try {
            UUID userUuid = UuidValidator.parseUuid(userId);
            
            return userAccountRepository.findById(userUuid)
                .onItem().ifNull().failWith(new UserNotFoundException(userId))
                .invoke(userAccount -> {
                    // Actualizar en Keycloak (bloqueante)
                    userKeycloakRepository.updateUser(userId, userUpdateDto);
                })
                .chain(userAccount -> {
                    // Obtener usuario actualizado de Keycloak
                    UserRepresentation keycloakUser = userKeycloakRepository.getUserById(userId);
                    
                    // Sincronizar en PostgreSQL
                    if (userUpdateDto.getUsername() != null) {
                        userAccount.setUsername(keycloakUser.getUsername());
                    }
                    if (userUpdateDto.getEmail() != null) {
                        userAccount.setEmail(keycloakUser.getEmail());
                    }
                    if (userUpdateDto.getFirstName() != null) {
                        userAccount.setFirstName(keycloakUser.getFirstName());
                    }
                    if (userUpdateDto.getLastName() != null) {
                        userAccount.setLastName(keycloakUser.getLastName());
                    }
                    
                    return userAccountRepository.persist(userAccount)
                        .invoke(() -> log.info("Usuario actualizado y sincronizado exitosamente: {}", userId))
                        .map(updated -> UserMapper.toResponseDto(updated, keycloakUser));
                })
                .onFailure().transform(error -> {
                    if (error instanceof UserNotFoundException ||
                        error instanceof InvalidUserDataException ||
                        error instanceof KeycloakSyncException) {
                        return error;
                    }
                    log.error("Error inesperado al actualizar usuario: {}", userId, error);
                    return new KeycloakSyncException("Error inesperado al actualizar usuario", error);
                });
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().failure(new InvalidUserDataException(e.getMessage()));
        }
    }

    @Override
    @WithTransaction
    public Uni<Void> deleteUser(String userId) {
        try {
            UUID userUuid = UuidValidator.parseUuid(userId);
            
            return Uni.createFrom().item(() -> {
                // 1. Eliminar de Keycloak (bloqueante)
                userKeycloakRepository.deleteUser(userId);
                return userId;
            })
            .chain(id -> {
                // 2. Eliminar de PostgreSQL (reactivo)
                return userAccountRepository.deleteById(userUuid)
                    .invoke(deleted -> {
                        if (!deleted) {
                            log.warn("Usuario eliminado de Keycloak pero no encontrado en PostgreSQL: {}", userId);
                        }
                        log.info("Usuario eliminado exitosamente: {}", userId);
                    })
                    .replaceWithVoid();
            })
            .onFailure().transform(error -> {
                if (error instanceof UserNotFoundException ||
                    error instanceof InvalidUserDataException ||
                    error instanceof KeycloakSyncException) {
                    return error;
                }
                log.error("Error inesperado al eliminar usuario: {}", userId, error);
                return new KeycloakSyncException("Error inesperado al eliminar usuario", error);
            });
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().failure(new InvalidUserDataException(e.getMessage()));
        }
    }

    @Override
    @WithSession
    public Uni<UserResponseDto> getUserById(String userId) {
        try {
            UUID userUuid = UuidValidator.parseUuid(userId);
            
            return userAccountRepository.findById(userUuid)
                .onItem().ifNull().failWith(new UserNotFoundException(userId))
                .map(userAccount -> {
                    // Obtener información actual de Keycloak
                    UserRepresentation keycloakUser = userKeycloakRepository.getUserById(userId);
                    return UserMapper.toResponseDto(userAccount, keycloakUser);
                })
                .onFailure().transform(error -> {
                    if (error instanceof UserNotFoundException ||
                        error instanceof InvalidUserDataException ||
                        error instanceof KeycloakSyncException) {
                        return error;
                    }
                    log.error("Error inesperado al obtener usuario: {}", userId, error);
                    return new KeycloakSyncException("Error inesperado al obtener usuario", error);
                });
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().failure(new InvalidUserDataException(e.getMessage()));
        }
    }

    @Override
    @WithSession
    public Uni<List<UserResponseDto>> getAllUsers(int page, int size) {
        // Validar parámetros de paginación
        if (page < 0 || size <= 0) {
            return Uni.createFrom().failure(new InvalidUserDataException(
                "Parámetros de paginación inválidos (page=" + page + ", size=" + size + ")"
            ));
        }
        
        return userAccountRepository.findAllPaginated(page, size)
            .map(userAccounts -> userAccounts.stream()
                .map(userAccount -> {
                    try {
                        UserRepresentation keycloakUser = userKeycloakRepository.getUserById(
                            userAccount.getId().toString()
                        );
                        return UserMapper.toResponseDto(userAccount, keycloakUser);
                    } catch (Exception e) {
                        log.warn("Error al obtener usuario de Keycloak: {}", userAccount.getId(), e);
                        return UserMapper.toResponseDto(userAccount, null);
                    }
                })
                .collect(Collectors.toList()))
            .onFailure().transform(error -> {
                log.error("Error al obtener usuarios", error);
                return new KeycloakSyncException("Error al obtener usuarios", error);
            });
    }

    @Override
    @WithSession
    public Uni<List<UserResponseDto>> searchUsersByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Uni.createFrom().failure(new InvalidUserDataException(
                "El username es requerido para la búsqueda"
            ));
        }
        
        return Uni.createFrom().item(() -> {
            // Buscar en Keycloak (bloqueante)
            return userKeycloakRepository.searchUsersByUsername(username);
        })
        .chain(keycloakUsers -> {
            // Convertir a DTOs obteniendo información de PostgreSQL
            List<Uni<UserResponseDto>> userUnis = keycloakUsers.stream()
                .map(keycloakUser -> {
                    try {
                        UUID userId = UuidValidator.parseUuid(keycloakUser.getId());
                        return userAccountRepository.findById(userId)
                            .map(userAccount -> {
                                if (userAccount == null) {
                                    log.warn("Usuario de Keycloak no encontrado en PostgreSQL: {}", userId);
                                    return UserMapper.toResponseDto(
                                        UserMapper.fromKeycloakUser(keycloakUser), 
                                        keycloakUser
                                    );
                                }
                                return UserMapper.toResponseDto(userAccount, keycloakUser);
                            });
                    } catch (Exception e) {
                        log.warn("Error al procesar usuario: {}", keycloakUser.getId(), e);
                        return Uni.createFrom().<UserResponseDto>nullItem();
                    }
                })
                .collect(Collectors.toList());
            
            return Uni.join().all(userUnis).andCollectFailures()
                .map(list -> list.stream()
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList()));
        })
        .onFailure().transform(error -> {
            log.error("Error al buscar usuarios", error);
            return new KeycloakSyncException("Error al buscar usuarios", error);
        });
    }

    @Override
    @WithTransaction
    public Uni<Void> updateProfileImage(String userId, byte[] profileImage) {
        try {
            UUID userUuid = UuidValidator.parseUuid(userId);
            
            return userAccountRepository.findById(userUuid)
                .onItem().ifNull().failWith(new UserNotFoundException(userId))
                .chain(userAccount -> {
                    // Validar tamaño de imagen (máx 5MB)
                    if (profileImage != null && profileImage.length > 5 * 1024 * 1024) {
                        return Uni.createFrom().failure(new InvalidUserDataException(
                            "La imagen de perfil no puede exceder 5MB (tamaño: " + profileImage.length + " bytes)"
                        ));
                    }
                    
                    return userAccountRepository.updateProfileImage(userUuid, profileImage)
                        .invoke(() -> log.info("Imagen de perfil actualizada para usuario: {}", userId))
                        .replaceWithVoid();
                })
                .onFailure().transform(error -> {
                    if (error instanceof UserNotFoundException ||
                        error instanceof InvalidUserDataException ||
                        error instanceof KeycloakSyncException) {
                        return error;
                    }
                    log.error("Error inesperado al actualizar imagen de perfil: {}", userId, error);
                    return new KeycloakSyncException("Error inesperado al actualizar imagen de perfil", error);
                });
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().failure(new InvalidUserDataException(e.getMessage()));
        }
    }

    @Override
    @WithSession
    public Uni<byte[]> getProfileImage(String userId) {
        try {
            UUID userUuid = UuidValidator.parseUuid(userId);
            
            return userAccountRepository.findById(userUuid)
                .onItem().ifNull().failWith(new UserNotFoundException(userId))
                .map(userAccount -> {
                    if (userAccount.getProfileImage() == null || userAccount.getProfileImage().length == 0) {
                        throw new UserNotFoundException("El usuario no tiene imagen de perfil");
                    }
                    return userAccount.getProfileImage();
                })
                .onFailure().transform(error -> {
                    if (error instanceof UserNotFoundException ||
                        error instanceof InvalidUserDataException ||
                        error instanceof KeycloakSyncException) {
                        return error;
                    }
                    log.error("Error inesperado al obtener imagen de perfil: {}", userId, error);
                    return new KeycloakSyncException("Error inesperado al obtener imagen de perfil", error);
                });
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().failure(new InvalidUserDataException(e.getMessage()));
        }
    }

    @Override
    public Uni<List<UserGroupResponseDto>> assignGroupsToUser(String userId, List<String> groupIds) {
        return Uni.createFrom().item(() -> {
            // Validar que el usuario existe
            userKeycloakRepository.getUserById(userId);
            
            // Asignar cada grupo
            for (String groupId : groupIds) {
                userKeycloakRepository.assignGroupToUser(userId, groupId);
            }
            
            log.info("Grupos asignados exitosamente al usuario: {}", userId);
            return userId;
        })
        .chain(id -> getUserGroups(id))
        .onFailure().transform(error -> {
            if (error instanceof UserNotFoundException ||
                error instanceof InvalidUserDataException ||
                error instanceof KeycloakSyncException) {
                return error;
            }
            log.error("Error inesperado al asignar grupos al usuario: {}", userId, error);
            return new KeycloakSyncException("Error inesperado al asignar grupos", error);
        });
    }

    @Override
    public Uni<List<UserGroupResponseDto>> removeGroupsFromUser(String userId, List<String> groupIds) {
        return Uni.createFrom().item(() -> {
            // Validar que el usuario existe
            userKeycloakRepository.getUserById(userId);
            
            // Remover cada grupo
            for (String groupId : groupIds) {
                userKeycloakRepository.removeGroupFromUser(userId, groupId);
            }
            
            log.info("Grupos removidos exitosamente del usuario: {}", userId);
            return userId;
        })
        .chain(id -> getUserGroups(id))
        .onFailure().transform(error -> {
            if (error instanceof UserNotFoundException ||
                error instanceof InvalidUserDataException ||
                error instanceof KeycloakSyncException) {
                return error;
            }
            log.error("Error inesperado al remover grupos del usuario: {}", userId, error);
            return new KeycloakSyncException("Error inesperado al remover grupos", error);
        });
    }

    @Override
    public Uni<List<UserGroupResponseDto>> getUserGroups(String userId) {
        return Uni.createFrom().item(() -> {
            var groups = userKeycloakRepository.getUserGroups(userId);
            return groups.stream()
                .map(group -> new UserGroupResponseDto(
                    group.getId(),
                    group.getName(),
                    group.getPath()
                ))
                .collect(Collectors.toList());
        })
        .onFailure().transform(error -> {
            if (error instanceof UserNotFoundException ||
                error instanceof InvalidUserDataException ||
                error instanceof KeycloakSyncException) {
                return error;
            }
            log.error("Error inesperado al obtener grupos del usuario: {}", userId, error);
            return new KeycloakSyncException("Error inesperado al obtener grupos", error);
        });
    }

    @Override
    public Uni<List<UserRoleResponseDto>> assignRolesToUser(String userId, List<String> roleNames) {
        return Uni.createFrom().item(() -> {
            // Validar que el usuario existe
            userKeycloakRepository.getUserById(userId);
            
            // Asignar cada rol
            for (String roleName : roleNames) {
                userKeycloakRepository.assignRoleToUser(userId, roleName);
            }
            
            log.info("Roles asignados exitosamente al usuario: {}", userId);
            return userId;
        })
        .chain(id -> getUserRoles(id))
        .onFailure().transform(error -> {
            if (error instanceof UserNotFoundException ||
                error instanceof InvalidUserDataException ||
                error instanceof KeycloakSyncException) {
                return error;
            }
            log.error("Error inesperado al asignar roles al usuario: {}", userId, error);
            return new KeycloakSyncException("Error inesperado al asignar roles", error);
        });
    }

    @Override
    public Uni<List<UserRoleResponseDto>> removeRolesFromUser(String userId, List<String> roleNames) {
        return Uni.createFrom().item(() -> {
            // Validar que el usuario existe
            userKeycloakRepository.getUserById(userId);
            
            // Remover cada rol
            for (String roleName : roleNames) {
                userKeycloakRepository.removeRoleFromUser(userId, roleName);
            }
            
            log.info("Roles removidos exitosamente del usuario: {}", userId);
            return userId;
        })
        .chain(id -> getUserRoles(id))
        .onFailure().transform(error -> {
            if (error instanceof UserNotFoundException ||
                error instanceof InvalidUserDataException ||
                error instanceof KeycloakSyncException) {
                return error;
            }
            log.error("Error inesperado al remover roles del usuario: {}", userId, error);
            return new KeycloakSyncException("Error inesperado al remover roles", error);
        });
    }

    @Override
    public Uni<List<UserRoleResponseDto>> getUserRoles(String userId) {
        return Uni.createFrom().item(() -> {
            var roles = userKeycloakRepository.getUserRoles(userId);
            return roles.stream()
                .map(role -> new UserRoleResponseDto(
                    role.getName(),
                    role.getDescription()
                ))
                .collect(Collectors.toList());
        })
        .onFailure().transform(error -> {
            if (error instanceof UserNotFoundException ||
                error instanceof InvalidUserDataException ||
                error instanceof KeycloakSyncException) {
                return error;
            }
            log.error("Error inesperado al obtener roles del usuario: {}", userId, error);
            return new KeycloakSyncException("Error inesperado al obtener roles", error);
        });
    }
}
