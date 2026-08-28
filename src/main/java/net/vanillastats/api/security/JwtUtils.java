package net.vanillastats.api.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public class JwtUtils {
    public static UUID getUserId(Jwt jwt) {
        String subject = jwt.getSubject();

        if (subject == null) {
            throw new InvalidTokenException("JWT is missing required 'sub' claim");
        }

        return UUID.fromString(subject);
    }
}
