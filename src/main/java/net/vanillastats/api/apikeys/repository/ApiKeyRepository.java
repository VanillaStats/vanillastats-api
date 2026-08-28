package net.vanillastats.api.apikeys.repository;

import net.vanillastats.api.apikeys.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    List<ApiKey> findByServerId(UUID serverId);
    Optional<ApiKey> findByKeyHash(String keyHash);
}
