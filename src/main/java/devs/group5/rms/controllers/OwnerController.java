package devs.group5.rms.controllers;

import devs.group5.rms.dtos.AssociateAdminRequest;
import devs.group5.rms.dtos.OwnerAdminAssociationResponse;
import devs.group5.rms.dtos.SummaryResponse;
import devs.group5.rms.services.JwtService;
import devs.group5.rms.services.OwnerService;
import devs.group5.rms.services.SummaryService;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Exposes owner-specific actions that require the authenticated owner's consent.
@RestController
@RequestMapping("/owners")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class OwnerController {
    private final OwnerService ownerService;
    private final SummaryService summaryService;
    private final JwtService jwtService;

    @GetMapping("/me/admin")
    public ResponseEntity<OwnerAdminAssociationResponse> getAssociatedAdmin(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ownerService.getAssociatedAdmin(UUID.fromString(jwt.getSubject()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/me/admin")
    public OwnerAdminAssociationResponse associateAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody AssociateAdminRequest request
    ) {
        val owner = ownerService.associateAdmin(
                UUID.fromString(jwt.getSubject()),
                request.adminId(),
                request.adminCut()
        );

        return new OwnerAdminAssociationResponse(
                owner.getId(),
                owner.getName(),
                owner.getAdmin().getId(),
                owner.getAdmin().getName(),
                owner.getAdminCut(),
                owner.getAdminAssociationAccepted()
        );
    }

    @GetMapping("/{ownerId}/summary")
    public SummaryResponse getSummary(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ownerId
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        return summaryService.getSummary(authenticatedUserId, authenticatedUserRole, ownerId);
    }
}
