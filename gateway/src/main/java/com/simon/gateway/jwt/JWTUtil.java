package com.simon.gateway.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JWTUtil {

    private final PublicKey publicKey;

    public JWTUtil() throws Exception {
        publicKey = getPublicKey();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token).getPayload();
    }

    private PublicKey getPublicKey() throws Exception {
        InputStream inputStream = JWTUtil.class.getClassLoader().getResourceAsStream("keys/public.pem");
        String key = (new String(inputStream.readAllBytes(), StandardCharsets.UTF_8))
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        inputStream.close();
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
