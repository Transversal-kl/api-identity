package com.fv.billpay.api.identity.exception;

/**
 * Excepción lanzada cuando hay un error de sincronización con Keycloak.
 */
public class KeycloakSyncException extends RuntimeException {
    
    public KeycloakSyncException(String message) {
        super("Error sincronizando con Keycloak: " + message);
    }
    
    public KeycloakSyncException(String message, Throwable cause) {
        super("Error sincronizando con Keycloak: " + message, cause);
    }
}
