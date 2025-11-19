package com.fv.billpay.api.identity.exception;

/**
 * Excepción lanzada cuando se intenta crear un usuario que ya existe.
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String username) {
        super("El usuario '" + username + "' ya existe");
    }
    
    public UserAlreadyExistsException(String username, Throwable cause) {
        super("El usuario '" + username + "' ya existe", cause);
    }
}
