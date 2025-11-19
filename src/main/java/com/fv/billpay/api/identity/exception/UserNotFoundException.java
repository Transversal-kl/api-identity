package com.fv.billpay.api.identity.exception;

/**
 * Excepción lanzada cuando no se encuentra un usuario.
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String userId) {
        super("Usuario no encontrado: " + userId);
    }
    
    public UserNotFoundException(String userId, Throwable cause) {
        super("Usuario no encontrado: " + userId, cause);
    }
}
