package net.vanillastats.api.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.UUID;

@Getter
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
    private final UUID serverId;

    public ApiKeyAuthenticationToken(UUID serverId) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.serverId = serverId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return serverId;
    }
}
