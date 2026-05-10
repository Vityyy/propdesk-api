package devs.group5.rms.controllers;

import devs.group5.rms.dtos.ApartmentWithTenantResponse;
import devs.group5.rms.dtos.MaintenanceFeeResponse;
import devs.group5.rms.dtos.PropertyRequest;
import devs.group5.rms.dtos.PropertyResponse;
import devs.group5.rms.dtos.TenantResponse;
import devs.group5.rms.entities.Role;
import devs.group5.rms.services.ApartmentService;
import devs.group5.rms.services.JwtService;
import devs.group5.rms.services.OwnerService;
import devs.group5.rms.services.AdminService;
import devs.group5.rms.services.PropertyService;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/properties")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class PropertyController {
    private final OwnerService ownerService;
    private final AdminService adminService;
    private final ApartmentService apartmentService;
    private final PropertyService propertyService;
    private final JwtService jwtService;

    @PostMapping
    public PropertyResponse addProperty(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody PropertyRequest request
    ) {
        val property = propertyService.addProperty(
                jwtService.extractUserId(jwt.getTokenValue()),
                jwtService.extractUserRole(jwt.getTokenValue()),
                request.propertyName(),
                request.propertyAddress(),
                request.pictureUrl(),
                request.ownerId(),
                request.apartmentRanges()
        );

        return new PropertyResponse(
                property.getId(),
                property.getName(),
                property.getAddress(),
                property.getImageUrl(),
                property.getOwner().getId()
        );
    }

    @GetMapping
    public List<PropertyResponse> getProperties(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "ownerId", required = false) String ownerIdParam
    ) {
        val userId = UUID.fromString(jwt.getSubject());
        val role = jwt.getClaimAsString("role");
        val isAdmin = "ADMIN".equals(role);

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
                            r.getImageUrl(),
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
                            r.getImageUrl(),
                            r.getOwner().getId()
                    ))
                    .collect(Collectors.toList());
        }

        return properties;
    }

    @DeleteMapping("/{propertyId}")
    public void deleteProperty(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID propertyId
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        propertyService.deleteProperty(authenticatedUserId, authenticatedUserRole, propertyId);
    }

    @GetMapping("/{propertyId}/apartments")
    public Map<Integer, Map<Integer, ApartmentWithTenantResponse>> getPropertyApartments(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID propertyId
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        val apartmentsByFloor = apartmentService.getApartmentsFromPropertyByFloor(authenticatedUserId, authenticatedUserRole, propertyId);

        return apartmentsByFloor.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry
                                .getValue()
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> mapApartmentResponse(e.getValue())
                                ))
                ));
    }

    @GetMapping("/apartments")
    public Map<UUID, Map<Integer, Map<Integer, ApartmentWithTenantResponse>>> getOwnerApartments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "ownerId", required = false) String ownerIdParam
    ) {
        val authenticatedUserId = jwtService.extractUserId(jwt.getTokenValue());
        val authenticatedUserRole = jwtService.extractUserRole(jwt.getTokenValue());
        UUID targetOwnerId;
        if (authenticatedUserRole == Role.ADMIN) {
            if (ownerIdParam == null || ownerIdParam.isBlank()) {
                throw new IllegalArgumentException("Admin must provide ownerId parameter");
            }
            targetOwnerId = UUID.fromString(ownerIdParam);
        } else {
            targetOwnerId = authenticatedUserId;
        }
        val apartmentsByProperty = apartmentService.getApartmentsFromOwnerGroupedByProperty(
                authenticatedUserId,
                authenticatedUserRole,
                targetOwnerId
        );

        return apartmentsByProperty.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        propertyEntry -> propertyEntry.getValue()
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        floorEntry -> floorEntry.getValue()
                                                .entrySet()
                                                .stream()
                                                .collect(Collectors.toMap(
                                                        Map.Entry::getKey,
                                                        apartmentEntry -> mapApartmentResponse(apartmentEntry.getValue())
                                                ))
                                ))
                ));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{propertyId}")
    public PropertyResponse updateProperty(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID propertyId,
            @RequestBody devs.group5.rms.dtos.PropertyUpdateRequest request
    ) {
        val property = propertyService.updateProperty(
                jwtService.extractUserId(jwt.getTokenValue()),
                jwtService.extractUserRole(jwt.getTokenValue()),
                propertyId,
                request.propertyName(),
                request.propertyAddress()
        );

        return new PropertyResponse(
                property.getId(),
                property.getName(),
                property.getAddress(),
                property.getImageUrl(),
                property.getOwner().getId()
        );
    }

    private ApartmentWithTenantResponse mapApartmentResponse(devs.group5.rms.data.ApartmentWithTenantData data) {
        TenantResponse tenantResponse = null;
        if (data.tenant() != null) {
            tenantResponse = new TenantResponse(
                    data.tenant().id(),
                    data.tenant().name(),
                    data.tenant().phone(),
                    data.tenant().email()
            );
        }

        val expenses = data.expenses().stream()
                .map(ex -> new devs.group5.rms.dtos.ApartmentExpenseResponse(
                        ex.id(), ex.amount(), ex.description()
                ))
                .toList();

        val maintenanceFees = data.maintenanceFees().stream()
                .map(f -> new MaintenanceFeeResponse(
                        f.id(), f.category(), f.description(), f.amount()
                ))
                .toList();

        return new ApartmentWithTenantResponse(
                data.id(),
                data.dueDate(),
                data.paymentStatus(),
                data.squareMeters(),
                data.rent(),
                tenantResponse,
                expenses,
                maintenanceFees
        );
    }
}
