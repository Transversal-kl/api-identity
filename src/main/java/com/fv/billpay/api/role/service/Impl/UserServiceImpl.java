package com.fv.billpay.api.role.service.Impl;

import com.fv.billpay.api.role.dto.request.UserRequestDto;
import com.fv.billpay.api.role.dto.request.UserUpdateDto;
import com.fv.billpay.api.role.dto.response.UserResponseDto;
import com.fv.billpay.api.role.entity.UserAccount;
import com.fv.billpay.api.role.mapper.UserMapper;
import com.fv.billpay.api.role.repository.IUserKeycloakRepository;
import com.fv.billpay.api.role.repository.UserAccountRepository;
import com.fv.billpay.api.role.service.IUserService;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
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
            if (error instanceof WebApplicationException) {
                return error;
            }
            log.error("Error al crear usuario", error);
            return new WebApplicationException(
                "Error al crear usuario: " + error.getMessage(),
                Response.Status.INTERNAL_SERVER_ERROR
            );
        });
    }

    @Override
    @WithTransaction
    public Uni<UserResponseDto> updateUser(String userId, UserUpdateDto userUpdateDto) {
        try {
            UUID userUuid = UUID.fromString(userId);
            
            return userAccountRepository.findById(userUuid)
                .onItem().ifNull().failWith(new WebApplicationException(
                    "Usuario con ID '" + userId + "' no encontrado en la base de datos",
                    Response.Status.NOT_FOUND
                ))
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
                    if (error instanceof WebApplicationException) {
                        return error;
                    }
                    log.error("Error al actualizar usuario: {}", userId, error);
                    return new WebApplicationException(
                        "Error al actualizar usuario: " + error.getMessage(),
                        Response.Status.INTERNAL_SERVER_ERROR
                    );
                });
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().failure(new WebApplicationException(
                "ID de usuario inválido: " + userId,
                Response.Status.BAD_REQUEST
            ));
        }
    }

    @Override
    @WithTransaction
    public Uni<Void> deleteUser(String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            
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
                if (error instanceof WebApplicationException) {
                    return error;
                }
                log.error("Error al eliminar usuario: {}", userId, error);
                return new WebApplicationException(
                    "Error al eliminar usuario: " + error.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR
                );
            });
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().failure(new WebApplicationException(
                "ID de usuario inválido: " + userId,
                Response.Status.BAD_REQUEST
            ));
        }
    }

    @Override
    public Uni<UserResponseDto> getUserById(String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            
            return userAccountRepository.findById(userUuid)
                .onItem().ifNull().failWith(new WebApplicationException(
                    "Usuario con ID '" + userId + "' no encontrado",
                    Response.Status.NOT_FOUND
                ))
                .map(userAccount -> {
                    // Obtener información actual de Keycloak
                    UserRepresentation keycloakUser = userKeycloakRepository.getUserById(userId);
                    return UserMapper.toResponseDto(userAccount, keycloakUser);
                })
                .onFailure().transform(error -> {
                    if (error instanceof WebApplicationException) {
                        return error;
                    }
                    log.error("Error al obtener usuario: {}", userId, error);
                    return new WebApplicationException(
                        "Error al obtener usuario: " + error.getMessage(),
                        Response.Status.INTERNAL_SERVER_ERROR
                    );
                });
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().failure(new WebApplicationException(
                "ID de usuario inválido: " + userId,
                Response.Status.BAD_REQUEST
            ));
        }
    }

    @Override
    public Uni<List<UserResponseDto>> getAllUsers(int page, int size) {
        // Validar parámetros de paginación
        if (page < 0 || size <= 0) {
            return Uni.createFrom().failure(new WebApplicationException(
                "Parámetros de paginación inválidos",
                Response.Status.BAD_REQUEST
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
                return new WebApplicationException(
                    "Error al obtener usuarios: " + error.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR
                );
            });
    }

    @Override
    public Uni<List<UserResponseDto>> searchUsersByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Uni.createFrom().failure(new WebApplicationException(
                "El username es requerido para la búsqueda",
                Response.Status.BAD_REQUEST
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
                        UUID userId = UUID.fromString(keycloakUser.getId());
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
            return new WebApplicationException(
                "Error al buscar usuarios: " + error.getMessage(),
                Response.Status.INTERNAL_SERVER_ERROR
            );
        });
    }

    @Override
    @WithTransaction
    public Uni<Void> updateProfileImage(String userId, byte[] profileImage) {
        try {
            UUID userUuid = UUID.fromString(userId);
            
            return userAccountRepository.findById(userUuid)
                .onItem().ifNull().failWith(new WebApplicationException(
                    "Usuario con ID '" + userId + "' no encontrado",
                    Response.Status.NOT_FOUND
                ))
                .chain(userAccount -> {
                    // Validar tamaño de imagen (máx 5MB)
                    if (profileImage != null && profileImage.length > 5 * 1024 * 1024) {
                        return Uni.createFrom().failure(new WebApplicationException(
                            "La imagen de perfil no puede exceder 5MB",
                            Response.Status.BAD_REQUEST
                        ));
                    }
                    
                    return userAccountRepository.updateProfileImage(userUuid, profileImage)
                        .invoke(() -> log.info("Imagen de perfil actualizada para usuario: {}", userId))
                        .replaceWithVoid();
                })
                .onFailure().transform(error -> {
                    if (error instanceof WebApplicationException) {
                        return error;
                    }
                    log.error("Error al actualizar imagen de perfil: {}", userId, error);
                    return new WebApplicationException(
                        "Error al actualizar imagen de perfil: " + error.getMessage(),
                        Response.Status.INTERNAL_SERVER_ERROR
                    );
                });
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().failure(new WebApplicationException(
                "ID de usuario inválido: " + userId,
                Response.Status.BAD_REQUEST
            ));
        }
    }

    @Override
    public Uni<byte[]> getProfileImage(String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            
            return userAccountRepository.findById(userUuid)
                .onItem().ifNull().failWith(new WebApplicationException(
                    "Usuario con ID '" + userId + "' no encontrado",
                    Response.Status.NOT_FOUND
                ))
                .map(userAccount -> {
                    if (userAccount.getProfileImage() == null || userAccount.getProfileImage().length == 0) {
                        throw new WebApplicationException(
                            "El usuario no tiene imagen de perfil",
                            Response.Status.NOT_FOUND
                        );
                    }
                    return userAccount.getProfileImage();
                })
                .onFailure().transform(error -> {
                    if (error instanceof WebApplicationException) {
                        return error;
                    }
                    log.error("Error al obtener imagen de perfil: {}", userId, error);
                    return new WebApplicationException(
                        "Error al obtener imagen de perfil: " + error.getMessage(),
                        Response.Status.INTERNAL_SERVER_ERROR
                    );
                });
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().failure(new WebApplicationException(
                "ID de usuario inválido: " + userId,
                Response.Status.BAD_REQUEST
            ));
        }
    }
}
