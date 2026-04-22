package devs.group5.rms.controllers;

import devs.group5.rms.data.PropertyData;
import devs.group5.rms.dtos.PropertyRequest;
import devs.group5.rms.dtos.PropertyResponse;
import devs.group5.rms.services.OwnerService;
import devs.group5.rms.services.AdminService;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/properties")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class PropertyController {
    private final OwnerService ownerService;
    private final AdminService adminService;

    @PostMapping
    public PropertyResponse addProperty(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody PropertyRequest request
    ) {
        val property = new PropertyData(request.name(), request.address(), request.ownerId());
        val response = ownerService.addProperty(
                UUID.fromString(jwt.getSubject()),
                property
        );


        return new PropertyResponse(
                response.getId(),
                response.getName(),
                response.getAddress(),
                response.getOwner().getId()
        );
    }

    @GetMapping
    public List<PropertyResponse> getProperties(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "ownerId", required = false) String ownerIdParam
    ) {
        val userId = UUID.fromString(jwt.getSubject());
        val roles = jwt.getClaimAsStringList("roles");
        val isAdmin = roles != null && roles.contains("ROLE_ADMIN");

        List<PropertyResponse> properties;

        if (isAdmin) {
            // Admin mode: get properties for specific owner (must be managed by this admin)
            if (ownerIdParam == null || ownerIdParam.isBlank()) {
                throw new RuntimeException("Admin must provide ownerId parameter");
            }
            val ownerId = UUID.fromString(ownerIdParam);
            val adminProperties = adminService.getOwnerProperties(userId, ownerId);
            properties = adminProperties.stream()
                    .map(r -> new PropertyResponse(
                            r.getId(),
                            r.getName(),
                            r.getAddress(),
                            r.getOwner().getId()
                    ))
                    .collect(Collectors.toList());
        } else {
            // Owner mode: get their own properties
            val ownerProperties = ownerService.getProperties(userId);
            properties = ownerProperties.stream()
                    .map(r -> new PropertyResponse(
                            r.getId(),
                            r.getName(),
                            r.getAddress(),
                            r.getOwner().getId()
                    ))
                    .collect(Collectors.toList());
        }

        return properties;
    }

    @DeleteMapping("/{propertyId}")
    public void deleteProperty(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID propertyId) {
        val ownerId = UUID.fromString(jwt.getSubject());
        ownerService.deleteProperty(ownerId, propertyId);
    }
}
