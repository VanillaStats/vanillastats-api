package net.vanillastats.api.apikeys.controller;

import net.vanillastats.api.apikeys.entity.ApiKey;
import net.vanillastats.api.apikeys.repository.ApiKeyRepository;
import net.vanillastats.api.apikeys.service.ApiKeyGenerator;
import net.vanillastats.api.config.SecurityConfig;
import net.vanillastats.api.servers.entity.Server;
import net.vanillastats.api.servers.repository.ServerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiKeyController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ApiKeyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServerRepository serverRepository;

    @MockitoBean
    private ApiKeyRepository apiKeyRepository;

    @MockitoBean
    private ApiKeyGenerator apiKeyGenerator;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();
    private final UUID serverId = UUID.randomUUID();

    @Test
    void createKey_returns201_whenCallerOwnsServer() throws Exception {
        Server server = new Server(ownerId, "My Server");
        ReflectionTestUtils.setField(server, "id", serverId);
        when(serverRepository.findById(serverId)).thenReturn(Optional.of(server));

        var generated = new ApiKeyGenerator.GeneratedKey("vs_live_rawkey123", "hashedvalue", "vs_live_raw");
        when(apiKeyGenerator.generate()).thenReturn(generated);

        ApiKey savedKey = new ApiKey(server.getId(), generated.hash(), generated.prefix());
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(savedKey);

        mockMvc.perform(post("/api/servers/{serverId}/keys", serverId)
                        .with(jwt().jwt(builder -> builder.subject(ownerId.toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("vs_live_rawkey123"))
                .andExpect(jsonPath("$.keyPrefix").value("vs_live_raw"));
    }

    @Test
    void createKey_returns404_whenCallerDoesNotOwnServer() throws Exception {
        Server server = new Server(ownerId, "My Server");
        when(serverRepository.findById(serverId)).thenReturn(Optional.of(server));

        mockMvc.perform(post("/api/servers/{serverId}/keys", serverId)
                        .with(jwt().jwt(builder -> builder.subject(otherUserId.toString()))))
                .andExpect(status().isNotFound());

        verify(apiKeyRepository, never()).save(any());
    }

    @Test
    void createKey_returns404_whenServerDoesNotExist() throws Exception {
        when(serverRepository.findById(serverId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/servers/{serverId}/keys", serverId)
                        .with(jwt().jwt(builder -> builder.subject(ownerId.toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void listKeys_returnsOnlyKeysForOwnedServer() throws Exception {
        Server server = new Server(ownerId, "My Server");
        when(serverRepository.findById(serverId)).thenReturn(Optional.of(server));

        ApiKey key = new ApiKey(server.getId(), "somehash", "vs_live_abc");
        when(apiKeyRepository.findByServerId(server.getId())).thenReturn(List.of(key));

        mockMvc.perform(get("/api/servers/{serverId}/keys", serverId)
                        .with(jwt().jwt(builder -> builder.subject(ownerId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].keyPrefix").value("vs_live_abc"))
                .andExpect(jsonPath("$[0].key").doesNotExist());
    }

    @Test
    void revokeKey_returns204_whenOwnedAndExists() throws Exception {
        Server server = new Server(ownerId, "My Server");
        ReflectionTestUtils.setField(server, "id", serverId);
        when(serverRepository.findById(serverId)).thenReturn(Optional.of(server));

        UUID keyId = UUID.randomUUID();
        ApiKey key = new ApiKey(server.getId(), "somehash", "vs_live_abc");
        when(apiKeyRepository.findById(keyId)).thenReturn(Optional.of(key));

        mockMvc.perform(delete("/api/servers/{serverId}/keys/{keyId}", serverId, keyId)
                        .with(jwt().jwt(builder -> builder.subject(ownerId.toString()))))
                .andExpect(status().isNoContent());

        verify(apiKeyRepository).save(key);
    }

    @Test
    void revokeKey_returns404_whenKeyBelongsToDifferentServer() throws Exception {
        Server server = new Server(ownerId, "My Server");
        when(serverRepository.findById(serverId)).thenReturn(Optional.of(server));

        UUID keyId = UUID.randomUUID();
        ApiKey key = new ApiKey(UUID.randomUUID(), "somehash", "vs_live_abc");
        when(apiKeyRepository.findById(keyId)).thenReturn(Optional.of(key));

        mockMvc.perform(delete("/api/servers/{serverId}/keys/{keyId}", serverId, keyId)
                        .with(jwt().jwt(builder -> builder.subject(ownerId.toString()))))
                .andExpect(status().isNotFound());
    }
}