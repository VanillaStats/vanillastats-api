package net.vanillastats.api.servers.dto;

import lombok.Builder;
import lombok.Getter;
import net.vanillastats.api.servers.entity.Server;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ServerResponse {
    private UUID id;
    private String name;
    private Instant createdAt;

    public static ServerResponse from(Server server) {
        return ServerResponse.builder()
                .id(server.getId())
                .name(server.getName())
                .createdAt(server.getCreatedAt())
                .build();
    }
}
