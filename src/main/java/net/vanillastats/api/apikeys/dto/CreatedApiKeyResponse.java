package net.vanillastats.api.apikeys.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class CreatedApiKeyResponse {
    private UUID id;
    private String key;
    private String keyPrefix;
    private Instant createdAt;
}
