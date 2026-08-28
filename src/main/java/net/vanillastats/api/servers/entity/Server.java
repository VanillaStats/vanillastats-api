package net.vanillastats.api.servers.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "servers")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Server {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Server(UUID ownerId, String name) {
        this.ownerId = ownerId;
        this.name = name;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
