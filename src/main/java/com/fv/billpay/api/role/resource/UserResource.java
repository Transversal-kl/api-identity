package com.fv.billpay.api.role.resource;

import com.fv.billpay.api.role.dto.request.UserRequestDto;
import com.fv.billpay.api.role.dto.request.UserUpdateDto;
import com.fv.billpay.api.role.dto.response.UserResponseDto;
import com.fv.billpay.api.role.service.IUserService;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;

/**
 * REST Resource reactivo para gestión de usuarios.
 * Sincroniza usuarios entre Keycloak y PostgreSQL.
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "Operaciones de gestión de usuarios")
@Slf4j
public class UserResource {

    @Inject
    IUserService userService;

    @POST
    @RolesAllowed({"admin", "billpay_user_update"})
    @Operation(summary = "Crear usuario", description = "Crea un usuario en Keycloak y lo sincroniza con PostgreSQL")
    @APIResponse(responseCode = "201", description = "Usuario creado exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "409", description = "El usuario ya existe")
    public Uni<Response> createUser(@Valid UserRequestDto userRequestDto) {
        log.info("Creando usuario: {}", userRequestDto.getUsername());
        return userService.createUser(userRequestDto)
            .map(user -> Response.status(Response.Status.CREATED).entity(user).build());
    }

    @PUT
    @Path("/{userId}")
    @RolesAllowed({"admin", "billpay_user_update"})
    @Operation(summary = "Actualizar usuario", description = "Actualiza un usuario en Keycloak y PostgreSQL")
    @APIResponse(responseCode = "200", description = "Usuario actualizado exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    public Uni<Response> updateUser(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("userId") String userId,
            @Valid UserUpdateDto userUpdateDto) {
        log.info("Actualizando usuario: {}", userId);
        return userService.updateUser(userId, userUpdateDto)
            .map(user -> Response.ok(user).build());
    }

    @DELETE
    @Path("/{userId}")
    @RolesAllowed({"admin", "billpay_user_update"})
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario de Keycloak y PostgreSQL")
    @APIResponse(responseCode = "200", description = "Usuario eliminado exitosamente")
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    public Uni<Response> deleteUser(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("userId") String userId) {
        log.info("Eliminando usuario: {}", userId);
        return userService.deleteUser(userId)
            .map(v -> Response.ok(java.util.Map.of("message", "Usuario eliminado exitosamente")).build());
    }

    @GET
    @Path("/{userId}")
    @RolesAllowed({"admin", "billpay_user_update", "viewer"})
    @Operation(summary = "Obtener usuario por ID", description = "Obtiene la información de un usuario")
    @APIResponse(
        responseCode = "200",
        description = "Usuario encontrado",
        content = @Content(schema = @Schema(implementation = UserResponseDto.class))
    )
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    public Uni<Response> getUserById(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("userId") String userId) {
        log.info("Obteniendo usuario: {}", userId);
        return userService.getUserById(userId)
            .map(user -> Response.ok(user).build());
    }

    @GET
    @RolesAllowed({"admin", "billpay_user_update", "viewer"})
    @Operation(summary = "Listar usuarios", description = "Obtiene todos los usuarios con paginación")
    @APIResponse(
        responseCode = "200",
        description = "Lista de usuarios",
        content = @Content(schema = @Schema(implementation = UserResponseDto.class))
    )
    public Uni<Response> getAllUsers(
            @Parameter(description = "Número de página (inicia en 0)")
            @QueryParam("page") @DefaultValue("0") int page,
            @Parameter(description = "Tamaño de página")
            @QueryParam("size") @DefaultValue("20") int size) {
        log.info("Listando usuarios - página: {}, tamaño: {}", page, size);
        return userService.getAllUsers(page, size)
            .map(users -> Response.ok(users).build());
    }

    @GET
    @Path("/search")
    @RolesAllowed({"admin", "billpay_user_update", "viewer"})
    @Operation(summary = "Buscar usuarios", description = "Busca usuarios por username")
    @APIResponse(
        responseCode = "200",
        description = "Resultados de búsqueda",
        content = @Content(schema = @Schema(implementation = UserResponseDto.class))
    )
    public Uni<Response> searchUsers(
            @Parameter(description = "Username a buscar", required = true)
            @QueryParam("username") String username) {
        log.info("Buscando usuarios por username: {}", username);
        return userService.searchUsersByUsername(username)
            .map(users -> Response.ok(users).build());
    }

    @PUT
    @Path("/{userId}/profile-image")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"admin", "billpay_user_update"})
    @Operation(summary = "Actualizar imagen de perfil", description = "Actualiza la imagen de perfil de un usuario")
    @APIResponse(responseCode = "200", description = "Imagen actualizada exitosamente")
    @APIResponse(responseCode = "400", description = "Imagen inválida o muy grande")
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    public Uni<Response> updateProfileImage(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("userId") String userId,
            @Parameter(description = "Archivo de imagen", required = true)
            @FormParam("image") FileUpload image) {
        log.info("Actualizando imagen de perfil para usuario: {}", userId);
        
        if (image == null) {
            throw new WebApplicationException(
                "La imagen es requerida",
                Response.Status.BAD_REQUEST
            );
        }
        
        return Uni.createFrom().item(() -> {
            // Leer bytes de la imagen
            try {
                return Files.readAllBytes(image.filePath());
            } catch (IOException e) {
                throw new WebApplicationException(
                    "Error al leer la imagen: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR
                );
            }
        })
        .chain(imageBytes -> userService.updateProfileImage(userId, imageBytes))
        .map(v -> Response.ok(java.util.Map.of("message", "Imagen de perfil actualizada exitosamente")).build());
    }

    @GET
    @Path("/{userId}/profile-image")
    @Produces({"image/jpeg", "image/png", "image/gif"})
    @RolesAllowed({"admin", "billpay_user_update", "viewer"})
    @Operation(summary = "Obtener imagen de perfil", description = "Obtiene la imagen de perfil de un usuario")
    @APIResponse(responseCode = "200", description = "Imagen de perfil")
    @APIResponse(responseCode = "404", description = "Usuario o imagen no encontrada")
    public Uni<Response> getProfileImage(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("userId") String userId) {
        log.info("Obteniendo imagen de perfil para usuario: {}", userId);
        return userService.getProfileImage(userId)
            .map(imageBytes -> Response.ok(imageBytes).build());
    }

    @DELETE
    @Path("/{userId}/profile-image")
    @RolesAllowed({"admin", "billpay_user_update"})
    @Operation(summary = "Eliminar imagen de perfil", description = "Elimina la imagen de perfil de un usuario")
    @APIResponse(responseCode = "200", description = "Imagen eliminada exitosamente")
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    public Uni<Response> deleteProfileImage(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("userId") String userId) {
        log.info("Eliminando imagen de perfil para usuario: {}", userId);
        return userService.updateProfileImage(userId, null)
            .map(v -> Response.ok(java.util.Map.of("message", "Imagen de perfil eliminada exitosamente")).build());
    }
}

