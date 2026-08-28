package net.vanillastats.api.servers.controller;

import net.vanillastats.api.config.SecurityConfig;
import net.vanillastats.api.servers.entity.Server;
import net.vanillastats.api.servers.repository.ServerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServerController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ServerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ServerRepository serverRepository;

    private final UUID testUserId = UUID.randomUUID();

    @Test
    void registerServer_returns200_withValidJwtAndBody() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", "Test Server"));

        Server savedServer = new Server(testUserId, "Test Server");
        when(serverRepository.save(any(Server.class))).thenReturn(savedServer);

        mockMvc.perform(post("/api/servers")
                        .with(jwt().jwt(builder -> builder.subject(testUserId.toString())))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Server"));

        verify(serverRepository).save(any(Server.class));
    }

    @Test
    void registerServer_returns400_whenNameBlank() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", ""));

        mockMvc.perform(post("/api/servers")
                        .with(jwt().jwt(builder -> builder.subject(testUserId.toString())))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(serverRepository, never()).save(any());
    }

    @Test
    void registerServer_returns401_withoutJwt() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", "Test Server"));

        mockMvc.perform(post("/api/servers")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());

        verify(serverRepository, never()).save(any());
    }

    @Test
    void listMyServers_returnsOnlyCallersServers() throws Exception {
        Server server = new Server(testUserId, "My Server");
        when(serverRepository.findByOwnerId(testUserId)).thenReturn(List.of(server));

        mockMvc.perform(get("/api/servers")
                        .with(jwt().jwt(builder -> builder.subject(testUserId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("My Server"));

        verify(serverRepository).findByOwnerId(testUserId);
    }
}
