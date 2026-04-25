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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/apartments")
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ApartmentController {
    private final OwnerService ownerService;

    @PostMapping
    public List<ApartmentResponse> addApartment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody List<ApartmentRequest> requests
    ) {
        var ownerId = UUID.fromString(jwt.getSubject());

        return requests.stream()
                .map(r -> {
                    val apartment = ownerService.addApartment(
                            ownerId,
                            new ApartmentData(r.number(), r.propertyId(), r.amount_due())
                    );
                    return new ApartmentResponse(apartment.getId(), apartment.getNumber(), apartment.getProperty().getId());
                })
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<ApartmentResponse> getApartments(@AuthenticationPrincipal Jwt jwt) {
        val ownerId = UUID.fromString(jwt.getSubject());
        return ownerService.getApartments(ownerId)
                .stream()
                .map(r -> new ApartmentResponse(r.getId(), r.getNumber(), r.getProperty().getId()))
                .collect(Collectors.toList());
    }
}
