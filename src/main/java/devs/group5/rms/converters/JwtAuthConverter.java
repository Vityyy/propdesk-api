package devs.group5.rms.converters;

import devs.group5.rms.entities.Role;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Role role = Role.valueOf(jwt.getClaimAsString("role"));
        val authority = new SimpleGrantedAuthority(role.asAuthority());
        return new JwtAuthenticationToken(jwt, List.of(authority));
    }
}
