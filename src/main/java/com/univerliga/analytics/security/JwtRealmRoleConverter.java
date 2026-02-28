package com.univerliga.analytics.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JwtRealmRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        Object realmAccess = source.getClaims().get("realm_access");
        List<?> roles = List.of();
        if (realmAccess instanceof Map<?, ?> map) {
            Object rawRoles = map.get("roles");
            if (rawRoles instanceof List<?> list) {
                roles = list;
            }
        }
        Collection<SimpleGrantedAuthority> authorities = roles.stream()
                .map(String::valueOf)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new JwtAuthenticationToken(source, authorities, source.getSubject());
    }
}
