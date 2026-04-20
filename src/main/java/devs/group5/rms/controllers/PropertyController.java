package devs.group5.rms.controllers;

import devs.group5.rms.data.PropertyData;
import devs.group5.rms.dtos.PropertyRequest;
import devs.group5.rms.dtos.PropertyResponse;
import devs.group5.rms.services.OwnerService;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/properties")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class PropertyController {
    private final OwnerService ownerService;

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
    public void deleteProperty(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID propertyId) {
        val ownerId = UUID.fromString(jwt.getSubject());
        ownerService.deleteProperty(ownerId, propertyId);
    }
}
