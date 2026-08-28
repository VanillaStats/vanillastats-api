package net.vanillastats.api.apikeys.dto;

import lombok.Builder;
import lombok.Getter;
import net.vanillastats.api.apikeys.entity.ApiKey;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ApiKeyResponse {
    private UUID id;
    private String keyPrefix;
    private Instant createdAt;
    private Instant revokedAt;

    public static ApiKeyResponse from(ApiKey apiKey) {
        return ApiKeyResponse.builder()
                .id(apiKey.getId())
                .keyPrefix(apiKey.getKeyPrefix())
                .createdAt(apiKey.getCreatedAt())
                .revokedAt(apiKey.getRevokedAt())
                .build();
    }
}
