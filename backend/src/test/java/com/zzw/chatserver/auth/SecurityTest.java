package com.zzw.chatserver.auth;

import com.zzw.chatserver.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityTest {

    @Test
    void createdTokenContainsUserIdentity() {
        String token = JwtUtils.createJwt("507f1f77bcf86cd799439011", "testuser");

        Claims claims = JwtUtils.parseJwt(token);

        assertEquals("507f1f77bcf86cd799439011", claims.getSubject());
        assertEquals("testuser", claims.get("username"));
        assertTrue(claims.getExpiration().getTime() > System.currentTimeMillis());
    }

    @Test
    void malformedTokenIsRejected() {
        assertThrows(JwtException.class, () -> JwtUtils.parseJwt("invalid-token"));
    }

    @Test
    void tokenCannotBeCreatedWithoutUserId() {
        assertThrows(IllegalArgumentException.class, () -> JwtUtils.createJwt(null, "testuser"));
    }
}
