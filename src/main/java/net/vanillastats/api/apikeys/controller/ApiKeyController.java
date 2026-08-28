package net.vanillastats.api.apikeys.controller;

import lombok.RequiredArgsConstructor;
import net.vanillastats.api.apikeys.dto.ApiKeyResponse;
import net.vanillastats.api.apikeys.dto.CreatedApiKeyResponse;
import net.vanillastats.api.apikeys.entity.ApiKey;
import net.vanillastats.api.apikeys.repository.ApiKeyRepository;
import net.vanillastats.api.apikeys.service.ApiKeyGenerator;
import net.vanillastats.api.security.JwtUtils;
import net.vanillastats.api.servers.entity.Server;
import net.vanillastats.api.servers.repository.ServerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/servers/{serverId}/keys")
@RequiredArgsConstructor
public class ApiKeyController {
    private final ServerRepository serverRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyGenerator apiKeyGenerator;

    @PostMapping
    public ResponseEntity<CreatedApiKeyResponse> createKey(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID serverId) {
        Server server = requireOwnedServer(jwt, serverId);

        ApiKeyGenerator.GeneratedKey generated = apiKeyGenerator.generate();
        ApiKey apiKey = new ApiKey(server.getId(), generated.hash(), generated.prefix());
        ApiKey saved = apiKeyRepository.save(apiKey);

        CreatedApiKeyResponse response = CreatedApiKeyResponse.builder()
                .id(saved.getId())
                .key(generated.rawKey())
                .keyPrefix(saved.getKeyPrefix())
                .createdAt(saved.getCreatedAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listKeys(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID serverId) {

        Server server = requireOwnedServer(jwt, serverId);

        List<ApiKeyResponse> keys = apiKeyRepository.findByServerId(server.getId())
                .stream()
                .map(ApiKeyResponse::from)
                .toList();

        return ResponseEntity.ok(keys);
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<Void> revokeKey(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID serverId,
            @PathVariable UUID keyId) {

        Server server = requireOwnedServer(jwt, serverId);

        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .filter(key -> key.getServerId().equals(server.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "API key not found"));

        apiKey.revoke();
        apiKeyRepository.save(apiKey);

        return ResponseEntity.noContent().build();
    }

    private Server requireOwnedServer(Jwt jwt, UUID serverId) {
        UUID userId = JwtUtils.getUserId(jwt);

        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Server not found"));

        if (!server.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Server not found");
        }

        return server;
    }
}
