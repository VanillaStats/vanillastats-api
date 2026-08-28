package net.vanillastats.api.security;

import jakarta.servlet.FilterChain;
import net.vanillastats.api.apikeys.entity.ApiKey;
import net.vanillastats.api.apikeys.repository.ApiKeyRepository;
import net.vanillastats.api.apikeys.service.ApiKeyGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationFilterTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private ApiKeyGenerator apiKeyGenerator;

    @Mock
    private FilterChain filterChain;

    private ApiKeyAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new ApiKeyAuthenticationFilter(apiKeyRepository, apiKeyGenerator);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void setsAuthentication_whenKeyValidAndNotRevoked() throws Exception {
        UUID serverId = UUID.randomUUID();
        when(apiKeyGenerator.hash("vs_live_validkey")).thenReturn("hashedvalue");

        ApiKey apiKey = new ApiKey(serverId, "hashedvalue", "vs_live_vali");
        when(apiKeyRepository.findByKeyHash("hashedvalue")).thenReturn(Optional.of(apiKey));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer vs_live_validkey");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        var auth = (ApiKeyAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getServerId()).isEqualTo(serverId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doesNotSetAuthentication_whenKeyNotFound() throws Exception {
        when(apiKeyGenerator.hash("vs_live_badkey")).thenReturn("hashedvalue");
        when(apiKeyRepository.findByKeyHash("hashedvalue")).thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer vs_live_badkey");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doesNotSetAuthentication_whenKeyRevoked() throws Exception {
        UUID serverId = UUID.randomUUID();
        when(apiKeyGenerator.hash("vs_live_revokedkey")).thenReturn("hashedvalue");

        ApiKey apiKey = new ApiKey(serverId, "hashedvalue", "vs_live_revo");
        apiKey.revoke();
        when(apiKeyRepository.findByKeyHash("hashedvalue")).thenReturn(Optional.of(apiKey));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer vs_live_revokedkey");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doesNotSetAuthentication_whenNoAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(apiKeyRepository);
    }
}
