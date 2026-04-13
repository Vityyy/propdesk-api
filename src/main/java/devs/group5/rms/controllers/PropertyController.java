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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/property")
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
                UUID.fromString(jwt.getId()),
                property
        );


        return new PropertyResponse(
                response.getId(),
                response.getName(),
                response.getAddress(),
                response.getOwner().getId()
        );
    }
}
