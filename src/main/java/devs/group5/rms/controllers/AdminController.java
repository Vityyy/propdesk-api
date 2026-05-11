package devs.group5.rms.controllers;

import devs.group5.rms.dtos.OwnerAssociationRequestResponse;
import devs.group5.rms.dtos.OwnerAdminAssociationResponse;
import devs.group5.rms.dtos.UserResponse;
import devs.group5.rms.services.AdminService;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// Exposes admin resources used by owners when granting management access.
@RestController
@RequestMapping("/admins")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class AdminController {
    private final AdminService adminService;

    // Lists registered admins so owners can pick one from the UI.
    @GetMapping
    public List<UserResponse> listAdmins() {
        val admins = adminService.getAdmins();

        return admins.stream()
                .map(admin -> new UserResponse(admin.getId(), admin.getName()))
                .toList();
    }

    // Lists all owners linked to the authenticated admin
    @GetMapping("/me/owners")
    public List<UserResponse> getMyOwners(
            @AuthenticationPrincipal Jwt jwt
    ) {
        val owners = adminService.getAdminOwners(UUID.fromString(jwt.getSubject()));

        return owners.stream()
                .map(owner -> new UserResponse(owner.getId(), owner.getName()))
                .toList();
    }

    // Lists pending owner association requests for the authenticated admin.
    @GetMapping("/me/owner-requests")
    public List<OwnerAssociationRequestResponse> getPendingOwnerRequests(
            @AuthenticationPrincipal Jwt jwt
    ) {
        val owners = adminService.getPendingOwnerRequests(UUID.fromString(jwt.getSubject()));

        return owners.stream()
                .map(owner -> new OwnerAssociationRequestResponse(
                        owner.getId(),
                        owner.getName(),
                        owner.getAdminCut()
                ))
                .toList();
    }

    // Accepts an owner association request for the authenticated admin.
    @PostMapping("/me/owners/{ownerId}/accept")
    public OwnerAdminAssociationResponse acceptOwnerRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ownerId
    ) {
        val owner = adminService.acceptOwnerRequest(
                UUID.fromString(jwt.getSubject()),
                ownerId
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

    // Rejects an owner association request for the authenticated admin.
    @DeleteMapping("/me/owner-requests/{ownerId}")
    public void rejectOwnerRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ownerId
    ) {
        adminService.rejectOwnerRequest(
                UUID.fromString(jwt.getSubject()),
                ownerId
        );
    }
}
