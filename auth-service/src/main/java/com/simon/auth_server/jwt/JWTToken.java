package com.simon.auth_server.jwt;

public record JWTToken(String type, String token) {

    public static JWTToken bearerType(String token) {
        return new JWTToken("Bearer", token);
    }
}
