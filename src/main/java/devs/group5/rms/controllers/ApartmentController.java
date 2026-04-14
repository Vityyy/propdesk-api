package devs.group5.rms.controllers;

import devs.group5.rms.data.ApartmentData;
import devs.group5.rms.dtos.ApartmentRequest;
import devs.group5.rms.dtos.ApartmentResponse;
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
@RequestMapping("/apartments")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ApartmentController {
    private final OwnerService ownerService;

    @PostMapping
    public ApartmentResponse addApartment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ApartmentRequest request
    ) {
        val apartment = ownerService.addApartment(
                UUID.fromString(jwt.getSubject()),
                new ApartmentData(request.name(), request.propertyId())
        );
        return new ApartmentResponse(apartment.getId(), apartment.getName(), apartment.getProperty().getId());
    }
}
