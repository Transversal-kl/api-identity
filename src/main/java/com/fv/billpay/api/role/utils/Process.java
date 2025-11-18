package com.fv.billpay.api.role.utils;

import jakarta.ws.rs.core.Response;

/**
 * Clase utilitaria para crear respuestas HTTP estandarizadas.
 * Todas las respuestas siguen el formato: { "status": "...", "data": ... }
 */
public final class Process {
    
    // Constructor privado para prevenir instanciación
    private Process() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no debe ser instanciada");
    }
    
    public static Response ok(Object data) {
        return Response.ok(new StandardResponse("success", data)).build();
    }
    
    public static Response error(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(new StandardResponse("error", message))
            .build();
    }
    
    public static Response notFound(String message) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity(new StandardResponse("not_found", message))
            .build();
    }
    
    /**
     * Clase interna que representa la estructura estándar de respuesta.
     */
    public static class StandardResponse {
        private final String status;
        private final Object data;
        
        public StandardResponse(String status, Object data) {
            this.status = status;
            this.data = data;
        }
        
        public String getStatus() {
            return status;
        }
        
        public Object getData() {
            return data;
        }
    }
}
