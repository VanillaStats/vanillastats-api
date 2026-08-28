package net.vanillastats.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilsTest {
    @Test
    void getUserId_returnsUuid_whenSubjectPresent() {
        UUID expected = UUID.randomUUID();
        Jwt jwt = buildJwt(expected.toString());

        UUID result = JwtUtils.getUserId(jwt);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getUserId_throwsInvalidTokenException_whenSubjectMissing() {
        Jwt jwt = buildJwt(null);

        assertThatThrownBy(() -> JwtUtils.getUserId(jwt))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("sub");
    }

    private Jwt buildJwt(String subject) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "ES256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claims(claims -> {
                    if (subject != null) {
                        claims.put("sub", subject);
                    } else {
                        claims.put("aud", "authenticated");
                    }
                });
        return builder.build();
    }
}
