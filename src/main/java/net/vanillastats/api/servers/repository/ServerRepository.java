package net.vanillastats.api.servers.repository;

import net.vanillastats.api.servers.entity.Server;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServerRepository extends JpaRepository<Server, UUID> {
    List<Server> findByOwnerId(UUID ownerId);
}
