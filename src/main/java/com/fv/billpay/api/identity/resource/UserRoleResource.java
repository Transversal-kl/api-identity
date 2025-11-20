package com.fv.billpay.api.identity.resource;

import com.fv.billpay.api.identity.dto.request.UserRolesRequestDto;
import com.fv.billpay.api.identity.service.IUserService;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST Resource para gestión de roles de usuarios.
 * Permite asignar, remover y consultar roles realm de usuarios en Keycloak.
 */
@Path("/users/{userId}/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User Roles", description = "Gestión de roles de usuarios")
@Slf4j
public class UserRoleResource {

    @Inject
    IUserService userService;

    @POST
    @RolesAllowed({"admin_users"})
    @Operation(
        summary = "Asignar roles a usuario", 
        description = "Asigna uno o más roles realm a un usuario en Keycloak"
    )
    @APIResponse(responseCode = "200", description = "Roles asignados exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "404", description = "Usuario o rol no encontrado")
    public Uni<Response> assignRolesToUser(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("userId") String userId,
            @Valid UserRolesRequestDto requestDto) {
        log.info("Asignando {} roles al usuario: {}", requestDto.getRoleNames().size(), userId);
        return userService.assignRolesToUser(userId, requestDto.getRoleNames())
            .map(roles -> Response.ok(roles).build());
    }

    @DELETE
    @RolesAllowed({"admin_users"})
    @Operation(
        summary = "Remover roles de usuario", 
        description = "Remueve uno o más roles realm de un usuario en Keycloak"
    )
    @APIResponse(responseCode = "200", description = "Roles removidos exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "404", description = "Usuario o rol no encontrado")
    public Uni<Response> removeRolesFromUser(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("userId") String userId,
            @Valid UserRolesRequestDto requestDto) {
        log.info("Removiendo {} roles del usuario: {}", requestDto.getRoleNames().size(), userId);
        return userService.removeRolesFromUser(userId, requestDto.getRoleNames())
            .map(roles -> Response.ok(roles).build());
    }

    @GET
    @RolesAllowed({"admin_users"})
    @Operation(
        summary = "Obtener roles de usuario", 
        description = "Obtiene todos los roles realm asignados a un usuario con paginación"
    )
    @APIResponse(responseCode = "200", description = "Lista paginada de roles del usuario")
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    public Uni<Response> getUserRoles(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("userId") String userId,
            @Parameter(description = "Número de página (inicia en 0)")
            @QueryParam("page") @DefaultValue("0") @Min(value = 0, message = "La página debe ser mayor o igual a 0") int page,
            @Parameter(description = "Tamaño de página")
            @QueryParam("size") @DefaultValue("10") @Min(value = 1, message = "El tamaño debe ser al menos 1") @Max(value = 100, message = "El tamaño máximo es 100") int size) {
        log.info("Obteniendo roles del usuario: {} - página: {}, tamaño: {}", userId, page, size);
        return userService.getUserRoles(userId, page, size)
            .map(pagedResponse -> Response.ok(pagedResponse).build());
    }
}
