package com.simon.auth_server.jwt;

import com.simon.auth_server.model.Role;
import com.simon.auth_server.model.User;
import io.jsonwebtoken.Jwts;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JWTUtil {

    private final PrivateKey privateKey;

    public JWTUtil() throws Exception {
        privateKey = getPrivateKey();
    }

    public JWTToken createToken(User user) {
        return JWTToken.bearerType(
                Jwts.builder()
                        .subject(user.getId().toString())
                        .claim("roles", user.getRoles().stream().map(Role::getName).toList())
                        .issuedAt(new Date())
                        .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hout
                        .signWith(privateKey, Jwts.SIG.RS256)
                        .compact());
    }

    private PrivateKey getPrivateKey() throws Exception {
        InputStream inputStream = JWTUtil.class.getClassLoader().getResourceAsStream("keys/private.pem");
        String key = (new String(inputStream.readAllBytes(), StandardCharsets.UTF_8))
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        inputStream.close();
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
}
