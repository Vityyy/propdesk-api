package devs.group5.rms.controllers;

import devs.group5.rms.services.JwtService;
import devs.group5.rms.services.TenantService;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tenants")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class TenantController {
    private final TenantService tenantService;
    private final JwtService jwtService;

    @DeleteMapping("/{tenantId}")
    public void deleteTenant(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tenantId,
            @RequestParam UUID ownerId
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        tenantService.deleteTenant(authenticatedUserId, authenticatedUserRole, tenantId, ownerId);
    }
}
