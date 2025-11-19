package com.fv.billpay.api.role.resource;

import com.fv.billpay.api.role.dto.request.UserGroupsRequestDto;
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
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST Resource para gestión de grupos de usuarios.
 * Permite asignar, remover y consultar grupos de usuarios en Keycloak.
 */
@Path("/users/{userId}/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User Groups", description = "Gestión de grupos de usuarios")
@Slf4j
public class UserGroupResource {

    @Inject
    IUserService userService;

    @POST
    @RolesAllowed({"admin_users"})
    @Operation(
        summary = "Asignar grupos a usuario", 
        description = "Asigna uno o más grupos a un usuario en Keycloak"
    )
    @APIResponse(responseCode = "200", description = "Grupos asignados exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "404", description = "Usuario o grupo no encontrado")
    public Uni<Response> assignGroupsToUser(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("userId") String userId,
            @Valid UserGroupsRequestDto requestDto) {
        log.info("Asignando {} grupos al usuario: {}", requestDto.getGroupIds().size(), userId);
        return userService.assignGroupsToUser(userId, requestDto.getGroupIds())
            .map(groups -> Response.ok(groups).build());
    }

    @DELETE
    @RolesAllowed({"admin_users"})
    @Operation(
        summary = "Remover grupos de usuario", 
        description = "Remueve uno o más grupos de un usuario en Keycloak"
    )
    @APIResponse(responseCode = "200", description = "Grupos removidos exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "404", description = "Usuario o grupo no encontrado")
    public Uni<Response> removeGroupsFromUser(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("userId") String userId,
            @Valid UserGroupsRequestDto requestDto) {
        log.info("Removiendo {} grupos del usuario: {}", requestDto.getGroupIds().size(), userId);
        return userService.removeGroupsFromUser(userId, requestDto.getGroupIds())
            .map(groups -> Response.ok(groups).build());
    }

    @GET
    @RolesAllowed({"admin_users"})
    @Operation(
        summary = "Obtener grupos de usuario", 
        description = "Obtiene todos los grupos asignados a un usuario"
    )
    @APIResponse(responseCode = "200", description = "Lista de grupos del usuario")
    @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    public Uni<Response> getUserGroups(
            @Parameter(description = "ID del usuario", required = true)
            @PathParam("userId") String userId) {
        log.info("Obteniendo grupos del usuario: {}", userId);
        return userService.getUserGroups(userId)
            .map(groups -> Response.ok(groups).build());
    }
}
