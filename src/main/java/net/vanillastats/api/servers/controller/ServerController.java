package net.vanillastats.api.servers.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.vanillastats.api.security.JwtUtils;
import net.vanillastats.api.servers.dto.CreateServerRequest;
import net.vanillastats.api.servers.dto.ServerResponse;
import net.vanillastats.api.servers.entity.Server;
import net.vanillastats.api.servers.repository.ServerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
public class ServerController {
    private final ServerRepository serverRepository;

    @PostMapping
    public ResponseEntity<ServerResponse> registerServer(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateServerRequest request) {
        UUID ownerId = JwtUtils.getUserId(jwt);
        Server server = new Server(ownerId, request.getName());
        Server saved = serverRepository.save(server);

        return ResponseEntity.ok(ServerResponse.from(saved));
    }

    @GetMapping
    public ResponseEntity<List<ServerResponse>> listMyServers(@AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = JwtUtils.getUserId(jwt);
        List<ServerResponse> servers = serverRepository.findByOwnerId(ownerId)
                .stream()
                .map(ServerResponse::from)
                .toList();

        return ResponseEntity.ok(servers);
    }
}
