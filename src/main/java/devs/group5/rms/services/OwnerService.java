package devs.group5.rms.services;

import devs.group5.rms.data.ApartmentData;
import devs.group5.rms.data.PropertyData;
import devs.group5.rms.entities.Apartment;
import devs.group5.rms.entities.Property;
import devs.group5.rms.repositories.ApartmentRepository;
import devs.group5.rms.repositories.OwnerRepository;
import devs.group5.rms.repositories.PropertyRepository;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class OwnerService {
    private final OwnerRepository ownerRepository;
    private final PropertyRepository propertyRepository;
    private final ApartmentRepository apartmentRepository;

    @PreAuthorize("hasRole('OWNER') and #data.ownerId().toString() == authentication.name")
    public Property addProperty(@NonNull PropertyData data) {
        var owner = ownerRepository.findById(data.ownerId()).orElseThrow(() -> new RuntimeException(""));
        var property = Property.builder()
                .name(data.name())
                .address(data.address())
                .owner(owner)
                .build();

        propertyRepository.save(property);
        return property;
    }

    @PreAuthorize("hasRole('OWNER')")
    public Apartment addApartment(UUID authenticatedUserId, @NonNull ApartmentData data) {
        var property = propertyRepository.findById(data.propertyId()).orElseThrow(() -> new RuntimeException(""));

        if (!property.getOwner().getId().equals(authenticatedUserId)) {
            throw new RuntimeException("Property does not belong to authenticated user");
        }

        var apartment = Apartment.builder()
                .name(data.name())
                .property(property)
                .build();

        apartmentRepository.save(apartment);
        return apartment;
    }
}
