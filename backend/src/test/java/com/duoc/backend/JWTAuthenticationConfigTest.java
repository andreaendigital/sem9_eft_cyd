package com.duoc.backend;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;

import static com.duoc.backend.Constants.SUPER_SECRET_KEY;
import static com.duoc.backend.Constants.TOKEN_BEARER_PREFIX;
import static com.duoc.backend.Constants.getSigningKey;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JWTAuthenticationConfigTest {

    private final JWTAuthenticationConfig config = new JWTAuthenticationConfig();

    /**
     * OWASP A07:2021 - Identification and Authentication Failures.
     * This test verifies that the token generated for an authenticated principal is a real JWS,
     * not a plaintext string, and that it can be verified with the backend signing key.
     */
    @Test
    void getJWTToken_ShouldGenerateSignedJwtWithThreeSegmentsAndBearerPrefix() {
        String token = config.getJWTToken("alice");

        assertNotNull(token);
        assertTrue(token.startsWith(TOKEN_BEARER_PREFIX));

        String rawToken = token.substring(TOKEN_BEARER_PREFIX.length());
        String[] segments = rawToken.split("\\.");
        assertEquals(3, segments.length, "A signed JWT must contain header, payload and signature");

        SecretKey key = (SecretKey) getSigningKey(SUPER_SECRET_KEY);
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(rawToken)
                .getPayload();

        assertNotNull(claims);
        assertEquals("alice", claims.getSubject());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new java.util.Date()), "Expiration must be in the future");

        long validityWindowMs = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertTrue(validityWindowMs >= 86_399_000L && validityWindowMs <= 86_402_000L,
                "Token lifetime should be coherently near the configured 24 hours");

        String headerJson = new String(Base64.getUrlDecoder().decode(segments[0]));
        assertFalse(headerJson.contains("\"alg\":\"none\""), "The token must not be a plaintext JWT");
    }

    /**
     * OWASP A07:2021 - Identification and Authentication Failures.
     * A null or empty principal must fail fast and safely so the system does not expose
     * internal details or continue generating an invalid authentication token.
     */
    @Test
    void getJWTToken_ShouldRejectNullAndEmptyUsername() {
        IllegalArgumentException nullException = assertThrows(IllegalArgumentException.class,
                () -> config.getJWTToken(null));
        assertEquals("Username is required", nullException.getMessage());

        IllegalArgumentException emptyException = assertThrows(IllegalArgumentException.class,
                () -> config.getJWTToken("   "));
        assertEquals("Username is required", emptyException.getMessage());
    }

    /**
     * Additional regression guard: ensures the method remains deterministic enough
     * for unit testing without SpringBootTest or a full application context.
     */
    @Test
    void getJWTToken_ShouldWorkWithoutSpringContext() {
        assertDoesNotThrow(() -> config.getJWTToken("bob"));
    }
}
