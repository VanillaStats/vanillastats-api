package net.vanillastats.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.vanillastats.api.apikeys.entity.ApiKey;
import net.vanillastats.api.apikeys.repository.ApiKeyRepository;
import net.vanillastats.api.apikeys.service.ApiKeyGenerator;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyGenerator apiKeyGenerator;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String rawKey = header.substring(7);
            String hash = apiKeyGenerator.hash(rawKey);

            Optional<ApiKey> apiKey = apiKeyRepository.findByKeyHash(hash)
                    .filter(key -> !key.isRevoked());

            if (apiKey.isPresent()) {
                var authentication = new ApiKeyAuthenticationToken(apiKey.get().getServerId());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
