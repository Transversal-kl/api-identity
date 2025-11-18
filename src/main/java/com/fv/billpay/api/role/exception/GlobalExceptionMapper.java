package com.fv.billpay.api.role.exception;

import com.fv.billpay.api.role.utils.Process;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Mapeador global de excepciones que convierte excepciones Java a respuestas HTTP apropiadas.
 * Maneja diferentes tipos de excepciones y las mapea a códigos HTTP correctos.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @ConfigProperty(name = "quarkus.profile", defaultValue = "prod")
    String profile;

    @Override
    public Response toResponse(Exception exception) {
        // Log de la excepción para debugging
        LOG.errorf(exception, "Excepción capturada: %s", exception.getClass().getSimpleName());

        // 1. Excepciones de validación (Bean Validation)
        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) exception;
            String violations = cve.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validación fallida");
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new Process.StandardResponse("validation_error", violations))
                .build();
        }

        // 2. Excepciones de JAX-RS (ya tienen código HTTP)
        if (exception instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) exception;
            int status = wae.getResponse().getStatus();
            String message = exception.getMessage();

            // Personalizar mensajes según el código HTTP
            if (exception instanceof NotFoundException) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new Process.StandardResponse("not_found", 
                        message != null ? message : "Recurso no encontrado"))
                    .build();
            }
            
            if (exception instanceof ForbiddenException) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(new Process.StandardResponse("forbidden", 
                        "No tienes permisos para acceder a este recurso"))
                    .build();
            }

            // Para otras WebApplicationException, usar el código que ya tienen
            return Response.status(status)
                .entity(new Process.StandardResponse("error", 
                    message != null ? message : "Error en la solicitud"))
                .build();
        }

        // 3. Excepciones de seguridad
        if (exception instanceof SecurityException || 
            exception.getClass().getName().contains("Unauthorized")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new Process.StandardResponse("unauthorized", 
                    "Autenticación requerida"))
                .build();
        }

        // 4. IllegalArgumentException y NullPointerException (errores de cliente)
        if (exception instanceof IllegalArgumentException || 
            exception instanceof NullPointerException) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new Process.StandardResponse("bad_request", 
                    exception.getMessage() != null ? exception.getMessage() : "Parámetros inválidos"))
                .build();
        }

        // 5. Cualquier otra excepción no controlada (500 Internal Server Error)
        String errorMessage = isDevelopment() 
            ? exception.getMessage() + " (" + exception.getClass().getSimpleName() + ")"
            : "Error interno del servidor";

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(new Process.StandardResponse("internal_error", errorMessage))
            .build();
    }

    private boolean isDevelopment() {
        return "dev".equalsIgnoreCase(profile) || "test".equalsIgnoreCase(profile);
    }
}
