package devs.group5.rms.controllers;

import devs.group5.rms.dtos.AssociateAdminRequest;
import devs.group5.rms.dtos.OwnerAdminAssociationResponse;
import devs.group5.rms.services.OwnerService;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// Exposes owner-specific actions that require the authenticated owner's consent.
@RestController
@RequestMapping("/owners")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class OwnerController {
    private final OwnerService ownerService;

    // Associates the authenticated owner with the chosen admin account.
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
                owner.getAdminCut()
        );
    }
}
