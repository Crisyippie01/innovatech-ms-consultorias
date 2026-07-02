package com.innovatech.ms_consultorias.security;

public record AuthenticatedUser(Long userId, String email, String role) {

    public boolean hasRole(String expectedRole) {
        return role != null && role.equalsIgnoreCase(expectedRole);
    }
}
