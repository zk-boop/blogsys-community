package com.zk.projectboot.ratelimit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zk.projectboot.model.User;
import com.zk.projectboot.security.BlogUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RateLimitFilter filter = new RateLimitFilter(
            new RateLimitService(Clock.fixed(Instant.parse("2026-07-15T00:00:00Z"), ZoneOffset.UTC)),
            objectMapper);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sixthRegistrationFromSameIpIsRejectedButAnotherIpIsAllowed() throws Exception {
        for (int i = 0; i < 5; i++) {
            assertEquals(200, perform("POST", "/api/auth/register", "127.0.0.1").getStatus());
        }

        MockHttpServletResponse limited = perform("POST", "/api/auth/register", "127.0.0.1");
        assertEquals(429, limited.getStatus());
        JsonNode body = objectMapper.readTree(limited.getContentAsString());
        assertEquals(false, body.get("success").asBoolean());
        assertEquals("操作过于频繁，请稍后重试", body.get("message").asText());

        assertEquals(200, perform("POST", "/api/auth/register", "127.0.0.2").getStatus());
    }

    @Test
    void authenticatedCommentLimitUsesPrincipalId() throws Exception {
        authenticate(3);
        for (int i = 0; i < 30; i++) {
            assertEquals(200, perform("POST", "/api/comments", "127.0.0.1").getStatus());
        }
        assertEquals(429, perform("POST", "/api/comments", "127.0.0.1").getStatus());

        authenticate(4);
        assertEquals(200, perform("POST", "/api/comments", "127.0.0.1").getStatus());
    }

    private void authenticate(int userId) {
        BlogUserPrincipal principal = new BlogUserPrincipal(
                userId, "user" + userId, "password", User.UserStatus.active, "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private MockHttpServletResponse perform(String method, String path, String remoteAddress) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr(remoteAddress);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
