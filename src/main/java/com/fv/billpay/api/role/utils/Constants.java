package com.fv.billpay.api.role.utils;

public class Constants {
    public static final String SQL_INSERT_ROLE = "INSERT INTO roles (name, description) VALUES (?, ?)";
    public static final String SQL_UPDATE_ROLE = "UPDATE roles SET name = ?, description = ? WHERE id = ?";
    public static final String SQL_DELETE_ROLE = "DELETE FROM roles WHERE id = ?";
    public static final String SQL_SELECT_ROLE_BY_ID = "SELECT id, name, description FROM roles WHERE id = ?";
    public static final String SQL_SELECT_ALL_ROLES = "SELECT id, name, description FROM roles OFFSET ? LIMIT ?";
    private Constants() {}
}
