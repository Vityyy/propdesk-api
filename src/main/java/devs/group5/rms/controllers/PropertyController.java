package devs.group5.rms.controllers;

import devs.group5.rms.dtos.ApartmentWithTenantResponse;
import devs.group5.rms.dtos.PropertyRequest;
import devs.group5.rms.dtos.PropertyResponse;
import devs.group5.rms.dtos.TenantResponse;
import devs.group5.rms.services.ApartmentService;
import devs.group5.rms.services.JwtService;
import devs.group5.rms.services.OwnerService;
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
                request.ownerId(),
                request.apartmentRanges()
        );

        return new PropertyResponse(
                property.getId(),
                property.getName(),
                property.getAddress(),
                property.getOwner().getId()
        );
    }

    @GetMapping
    public List<PropertyResponse> getProperties(@AuthenticationPrincipal Jwt jwt) {
        val ownerId = UUID.fromString(jwt.getSubject());
        return ownerService.getProperties(ownerId)
                .stream()
                .map(r -> new PropertyResponse(
                        r.getId(),
                        r.getName(),
                        r.getAddress(),
                        r.getOwner().getId()
                ))
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{propertyId}")
    public void deleteProperty(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID propertyId
    ) {
        val ownerId = UUID.fromString(jwt.getSubject());
        ownerService.deleteProperty(ownerId, propertyId);
    }

    @GetMapping("/{propertyId}/apartments")
    public Map<Integer, Map<Integer, ApartmentWithTenantResponse>> getPropertyApartments(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID propertyId
    ) {
        val apartmentsByFloor = apartmentService.getApartmentsFromPropertyByFloor(propertyId);

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
            @AuthenticationPrincipal Jwt jwt
    ) {
        val ownerId = UUID.fromString(jwt.getSubject());
        val apartmentsByProperty = apartmentService.getApartmentsFromOwnerGroupedByProperty(ownerId);

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

        return new ApartmentWithTenantResponse(
                data.id(),
                data.dueDate(),
                data.paymentStatus(),
                data.squareMeters(),
                data.rent(),
                tenantResponse,
                expenses
        );
    }
}
